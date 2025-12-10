import {expect} from '@loopback/testlab';
import {ChallengeService} from '../../../services';
import {
  BadgeRepository,
  ChallengeRepository,
  ExplorationEventRepository,
  UserBadgeRepository,
  UserChallengeRepository,
  UserStreakRepository,
} from '../../../repositories';
import {Challenge, ExplorationEvent, UserChallenge} from '../../../models';

class TestChallengeService extends ChallengeService {
  public evaluateCondition(
    userId: number,
    challenge: Challenge,
    userChallenge: UserChallenge,
  ): Promise<boolean> {
    return this.evaluateChallengeCondition(userId, challenge, userChallenge);
  }
}

class FakeExplorationEventRepository {
  constructor(private readonly events: ExplorationEvent[]) {}

  async count(filter?: any) {
    const where = this.extractWhere(filter);
    return {count: this.filterEvents(where).length};
  }

  async find(filter?: any) {
    const where = this.extractWhere(filter);
    return this.filterEvents(where);
  }

  private extractWhere(filter?: any) {
    if (!filter) {
      return {};
    }
    return filter.where ?? filter;
  }

  private filterEvents(where: any): ExplorationEvent[] {
    return this.events.filter(event => {
      if (where.userId != null && event.userId !== where.userId) {
        return false;
      }
      if (!where.completedAt) {
        return true;
      }
      const completedAt = event.completedAt ? new Date(event.completedAt) : undefined;
      if (!completedAt) {
        return false;
      }
      const comparisons = where.completedAt;
      if (comparisons.gte && completedAt < new Date(comparisons.gte)) {
        return false;
      }
      if (comparisons.lte && completedAt > new Date(comparisons.lte)) {
        return false;
      }
      if (Array.isArray(comparisons.between)) {
        const [from, to] = comparisons.between.map((value: Date) => new Date(value));
        if (completedAt < from || completedAt > to) {
          return false;
        }
      }
      return true;
    });
  }
}

class FakeUserStreakRepository {
  constructor(private readonly streak?: number) {}

  async findOne(): Promise<{currentStreak: number} | null> {
    if (typeof this.streak !== 'number') {
      return null;
    }
    return {currentStreak: this.streak};
  }
}

function createService(events: ExplorationEvent[], streak?: number): TestChallengeService {
  return new TestChallengeService(
    {} as ChallengeRepository,
    {} as UserChallengeRepository,
    new FakeExplorationEventRepository(events) as unknown as ExplorationEventRepository,
    new FakeUserStreakRepository(streak) as unknown as UserStreakRepository,
    {} as BadgeRepository,
    {} as UserBadgeRepository,
  );
}

function createChallenge(overrides: Partial<Challenge>): Challenge {
  return {
    id: overrides.id ?? 1,
    name: overrides.name ?? 'Test Challenge',
    description: overrides.description ?? 'Test description',
    iconUrl: overrides.iconUrl,
    conditionType: overrides.conditionType ?? 'exploration_count',
    conditionParams: overrides.conditionParams ?? {count: 1},
    rewardBadgeId: overrides.rewardBadgeId ?? 1,
    cooldownHours: overrides.cooldownHours ?? 24,
    expirationHours: overrides.expirationHours ?? 24,
    allowsExtension: overrides.allowsExtension ?? false,
    isActive: overrides.isActive ?? true,
    difficulty: overrides.difficulty ?? 'easy',
    displayOrder: overrides.displayOrder ?? 0,
    createdAt: overrides.createdAt,
  } as Challenge;
}

function createUserChallenge(overrides: Partial<UserChallenge>): UserChallenge {
  return {
    id: overrides.id ?? 1,
    userId: overrides.userId ?? 1,
    challengeId: overrides.challengeId ?? 1,
    status: overrides.status ?? 'active',
    activatedAt: overrides.activatedAt ?? new Date(),
    completedAt: overrides.completedAt,
    expiresAt: overrides.expiresAt,
    cooldownEndsAt: overrides.cooldownEndsAt,
    progressData: overrides.progressData,
    notificationSent: overrides.notificationSent,
  } as UserChallenge;
}

describe('ChallengeService.evaluateChallengeCondition', () => {
  it('counts only explorations inside the activation window', async () => {
    const now = Date.now();
    const activatedAt = new Date(now - 3 * 60 * 60 * 1000);
    const expiresAt = new Date(now - 60 * 60 * 1000);

    const events: ExplorationEvent[] = [
      {id: 1, userId: 1, completedAt: new Date(activatedAt.getTime() - 10 * 60 * 1000)} as ExplorationEvent,
      {id: 2, userId: 1, completedAt: new Date(activatedAt.getTime() + 30 * 60 * 1000)} as ExplorationEvent,
      {id: 3, userId: 1, completedAt: new Date(expiresAt.getTime() + 30 * 60 * 1000)} as ExplorationEvent,
    ];

    const service = createService(events);
    const userChallenge = createUserChallenge({activatedAt, expiresAt});

    const strictChallenge = createChallenge({
      conditionType: 'exploration_count',
      conditionParams: {count: 2},
    });
    const relaxedChallenge = createChallenge({
      conditionType: 'exploration_count',
      conditionParams: {count: 1},
    });

    const strictResult = await service.evaluateCondition(1, strictChallenge, userChallenge);
    const relaxedResult = await service.evaluateCondition(1, relaxedChallenge, userChallenge);

    expect(strictResult).to.be.false();
    expect(relaxedResult).to.be.true();
  });

  it('honors UTC hours for time-based challenges', async () => {
    const activatedAt = new Date('2024-01-01T00:00:00Z');
    const expiresAt = new Date('2024-01-02T06:00:00Z');
    const eventTime = new Date('2024-01-01T22:15:00-05:00'); // 03:15 UTC within window

    const events: ExplorationEvent[] = [
      {id: 1, userId: 1, completedAt: eventTime} as ExplorationEvent,
    ];

    const service = createService(events);
    const userChallenge = createUserChallenge({activatedAt, expiresAt});
    const challenge = createChallenge({
      conditionType: 'time_based',
      conditionParams: {hour: 27}, // normalized to 3 UTC
    });

    const result = await service.evaluateCondition(1, challenge, userChallenge);

    expect(result).to.be.true();
  });

  it('rejects time-based progress outside the activation window', async () => {
    const activatedAt = new Date('2024-01-01T00:00:00Z');
    const expiresAt = new Date('2024-01-02T00:00:00Z');
    const lateEvent = new Date('2024-01-01T22:15:00-05:00'); // 03:15 UTC after expiry

    const events: ExplorationEvent[] = [
      {id: 1, userId: 1, completedAt: lateEvent} as ExplorationEvent,
    ];

    const service = createService(events);
    const userChallenge = createUserChallenge({activatedAt, expiresAt});
    const challenge = createChallenge({
      conditionType: 'time_based',
      conditionParams: {hour: 27},
    });

    const result = await service.evaluateCondition(1, challenge, userChallenge);
    expect(result).to.be.false();
  });

    const now = Date.now();
    const activatedAt = new Date(now - 6 * 60 * 60 * 1000);
    const expiresAt = new Date(now - 4 * 60 * 60 * 1000);
    const lateEvent = new Date(now - 2 * 60 * 60 * 1000);

    const events: ExplorationEvent[] = [
      {id: 1, userId: 1, completedAt: lateEvent} as ExplorationEvent,
    ];

    const service = createService(events);
    const userChallenge = createUserChallenge({activatedAt, expiresAt});
    const challenge = createChallenge({
      conditionType: 'exploration_count',
      conditionParams: {count: 1},
    });

    const result = await service.evaluateCondition(1, challenge, userChallenge);

    expect(result).to.be.false();
  });

  it('does not count progress when challenge is not active', async () => {
    const activatedAt = new Date('2024-01-01T00:00:00Z');
    const expiresAt = new Date('2024-01-01T01:00:00Z');

    const events: ExplorationEvent[] = [
      {id: 1, userId: 1, completedAt: new Date('2024-01-01T00:30:00Z')} as ExplorationEvent,
    ];

    const service = createService(events);
    const userChallenge = createUserChallenge({
      activatedAt,
      expiresAt,
      status: 'expired',
    });
    const challenge = createChallenge({
      conditionType: 'exploration_count',
      conditionParams: {count: 1},
    });

    const result = await service.evaluateCondition(1, challenge, userChallenge);
    expect(result).to.be.false();
  });
});

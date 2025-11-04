import {injectable, BindingScope} from '@loopback/core';
import {repository} from '@loopback/repository';
import {UserStreakRepository} from '../repositories';
import {UserStreak} from '../models';

@injectable({scope: BindingScope.TRANSIENT})
export class StreakCalculatorService {
  constructor(
    @repository(UserStreakRepository)
    public userStreakRepository: UserStreakRepository,
  ) {}

  /**
   * Update user's streak based on new exploration event
   * @param userId - User ID
   * @param eventDate - Date of exploration (defaults to today)
   */
  async updateStreak(userId: number, eventDate: Date = new Date()): Promise<UserStreak> {
    let userStreak = await this.userStreakRepository.findOne({
      where: {userId}
    });

    if (!userStreak) {
      userStreak = await this.userStreakRepository.create({
        userId,
        currentStreak: 1,
        longestStreak: 1,
        lastActivityDate: eventDate,
        updatedAt: new Date(),
      });
      return userStreak;
    }

    // Check if lastActivityDate exists before processing
    if (!userStreak.lastActivityDate) {
      userStreak.currentStreak = 1;
      userStreak.longestStreak = Math.max(userStreak.longestStreak ?? 0, 1);
      userStreak.lastActivityDate = eventDate;
      userStreak.updatedAt = new Date();
      await this.userStreakRepository.updateById(userStreak.id!, userStreak);
      return userStreak;
    }

    const lastDate = this.toDateOnly(userStreak.lastActivityDate);
    const eventDateOnly = this.toDateOnly(eventDate);

    // Calculate days difference (positive = event is after last date)
    const diffDays = this.diffInDays(eventDateOnly, lastDate);

    // Already logged for this day - no change
    if (diffDays === 0) {
      return userStreak;
    }

    // Event is older than last recorded activity - ignore
    if (diffDays < 0) {
      return userStreak;
    }

    // Consecutive day (exactly 1 day after) - increment streak
    if (diffDays === 1) {
      userStreak.currentStreak = (userStreak.currentStreak ?? 0) + 1;
      userStreak.longestStreak = Math.max(
        userStreak.longestStreak ?? 0,
        userStreak.currentStreak
      );
    } else {
      // Gap in activity - reset streak to 1
      userStreak.currentStreak = 1;
    }

    userStreak.lastActivityDate = eventDate;
    userStreak.updatedAt = new Date();

    // Save to database
    await this.userStreakRepository.updateById(userStreak.id!, userStreak);

    return userStreak;
  }

  /**
   * Convert Date to midnight (strips time component)
   */
  private toDateOnly(date: Date): Date {
    return new Date(date.getFullYear(), date.getMonth(), date.getDate());
  }

  /**
   * Calculate difference in days between two dates (dateA - dateB)
   */
  private diffInDays(dateA: Date, dateB: Date): number {
    const msPerDay = 24 * 60 * 60 * 1000;
    const utcA = Date.UTC(dateA.getFullYear(), dateA.getMonth(), dateA.getDate());
    const utcB = Date.UTC(dateB.getFullYear(), dateB.getMonth(), dateB.getDate());
    return Math.floor((utcA - utcB) / msPerDay);
  }
}

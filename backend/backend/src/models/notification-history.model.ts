import {Entity, model, property} from '@loopback/repository';

@model()
export class NotificationHistory extends Entity {
  @property({
    type: 'number',
    id: true,
    generated: true,
  })
  id?: number;

  @property({
    type: 'number',
    required: true,
    index: true,
  })
  userId: number;

  @property({
    type: 'string',
    required: true,
  })
  notificationType: string; // 'doing_well', 'skipping', 'streak_reminder'

  @property({
    type: 'string',
  })
  title?: string;

  @property({
    type: 'string',
  })
  body?: string;

  @property({
    type: 'date',
    default: () => new Date(),
  })
  sentAt?: Date;

  @property({
    type: 'boolean',
    default: false,
  })
  wasDismissed?: boolean;

  @property({
    type: 'date',
  })
  dismissedAt?: Date;

  constructor(data?: Partial<NotificationHistory>) {
    super(data);
  }
}

export interface NotificationHistoryRelations {
  // describe navigational properties here
}

export type NotificationHistoryWithRelations = NotificationHistory & NotificationHistoryRelations;

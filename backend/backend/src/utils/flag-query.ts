import {Filter} from '@loopback/repository';
import {Flag} from '../models';

const DEFAULT_FLAG_FIELDS = {
  id: true,
  location_id: true,
  photoCode: true,
  dateTaken: true,
} as const;

export function applyDefaultFlagFields(filter: Filter<Flag> = {}): Filter<Flag> {
  if (filter.fields) {
    return filter;
  }

  return {
    ...filter,
    fields: DEFAULT_FLAG_FIELDS,
  };
}

export function getFlagQueryTimeoutMs(): number {
  const parsed = Number(process.env.FLAG_QUERY_TIMEOUT_MS ?? 3000);
  if (Number.isFinite(parsed) && parsed > 0) {
    return parsed;
  }

  return 3000;
}

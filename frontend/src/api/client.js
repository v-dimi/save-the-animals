import { animalImageUrl } from './animals.js';

/** Every failure the UI can show arrives as one of these. */
export class ApiError extends Error {
  constructor(message, status) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
  }
}

const EXTENSIONS = {
  'image/jpeg': 'jpg',
  'image/png': 'png',
  'image/gif': 'gif',
  'image/webp': 'webp',
};

/** Unpacks an RFC 9457 problem document */
async function errorFrom(response) {
  let detail = null;
  try {
    const problem = await response.json();
    detail = problem.detail ?? problem.title ?? null;
  } catch {
    // Not a JSON body; the status is all we have.
  }
  return new ApiError(detail ?? `The server answered ${response.status}.`, response.status);
}

/**
 * Actuator answers 503 with a body when a component is down, so the body is read either way.
 */
export async function checkHealth() {
  const response = await fetch('/actuator/health', { headers: { Accept: 'application/json' } });

  let report = null;
  try {
    report = await response.json();
  } catch {
    // A proxy error page rather than Actuator.
  }

  const status = report?.status ?? (response.ok ? 'UP' : 'DOWN');
  return {
    up: response.ok && status === 'UP',
    status,
    database: report?.components?.db?.status ?? null,
  };
}

/** Fetches a random picture through the proxy, and keeps the bytes so it can be saved. */
export async function fetchAnimalImage(animal) {
  const sourceUrl = animalImageUrl(animal);
  const response = await fetch(sourceUrl);
  if (!response.ok) {
    throw new ApiError(`The ${animal} service answered ${response.status}.`, response.status);
  }

  const blob = await response.blob();
  if (blob.type && !blob.type.startsWith('image/')) {
    throw new ApiError(`The ${animal} service sent ${blob.type} instead of an image.`, response.status);
  }
  return { blob, sourceUrl, animal };
}

/** Uploads the bytes currently on screen. Resolves to the stored image's metadata. */
export async function saveImage({ blob, animal, sourceUrl }) {
  const form = new FormData();
  form.append('file', blob, `${animal}.${EXTENSIONS[blob.type] ?? 'bin'}`);
  form.append('animal', animal);
  if (sourceUrl) {
    form.append('sourceUrl', sourceUrl);
  }

  const response = await fetch('/api/images', { method: 'POST', body: form });
  if (!response.ok) {
    throw await errorFrom(response);
  }
  return response.json();
}

/** The most recently stored image, or null when nothing has been stored yet. */
export async function fetchLatestImage() {
  const response = await fetch('/api/images/latest');
  if (response.status === 404) {
    return null;
  }
  if (!response.ok) {
    throw await errorFrom(response);
  }
  return response.json();
}

/** Where the browser can load a stored image's bytes from. */
export function contentUrl(id) {
  return `/api/images/${id}/content`;
}

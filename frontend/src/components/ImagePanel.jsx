import { labelFor } from '../api/animals.js';

const formatSize = (bytes) =>
  typeof bytes === 'number' ? `${(bytes / 1024).toFixed(1)} KB` : null;

const formatTime = (iso) => {
  if (!iso) {
    return null;
  }
  const parsed = new Date(iso);
  return Number.isNaN(parsed.getTime()) ? null : parsed.toLocaleString();
};

export default function ImagePanel({ image, loading }) {
  if (loading) {
    return (
      <div className="panel panel--placeholder" data-testid="image-panel">
        Fetching a picture…
      </div>
    );
  }

  if (!image) {
    return (
      <div className="panel panel--placeholder" data-testid="image-panel">
        No picture yet. Pick an animal, or load the last one you saved.
      </div>
    );
  }

  const facts = [
    image.id ? `stored as #${image.id}` : 'not saved yet',
    formatSize(image.sizeBytes),
    formatTime(image.createdAt),
  ].filter(Boolean);

  return (
    <figure className="panel" data-testid="image-panel">
      <img src={image.src} alt={`A random ${image.animal}`} />
      <figcaption>
        <strong>{labelFor(image.animal)}</strong>
        <span>{facts.join(' · ')}</span>
        {image.sourceUrl ? <code>{image.sourceUrl}</code> : null}
      </figcaption>
    </figure>
  );
}

import { useEffect, useRef, useState } from 'react';

import AnimalButtons from './components/AnimalButtons.jsx';
import HealthButton from './components/HealthButton.jsx';
import ImagePanel from './components/ImagePanel.jsx';
import StatusBanner from './components/StatusBanner.jsx';
import { ANIMAL_IDS } from './api/animals.js';
import { contentUrl, fetchAnimalImage, fetchLatestImage, saveImage } from './api/client.js';

export default function App() {
  const [image, setImage] = useState(null);
  const [busy, setBusy] = useState(null);
  const [status, setStatus] = useState(null);

  // Object URLs are released when they are replaced rather than in an effect cleanup:
  // StrictMode invokes effect cleanups on the first mount, which would revoke the URL of
  // the picture currently on screen.
  const liveObjectUrl = useRef(null);

  useEffect(
    () => () => {
      if (liveObjectUrl.current) {
        URL.revokeObjectURL(liveObjectUrl.current);
        liveObjectUrl.current = null;
      }
    },
    [],
  );

  function show(next) {
    if (liveObjectUrl.current) {
      URL.revokeObjectURL(liveObjectUrl.current);
    }
    liveObjectUrl.current = next.objectUrl ?? null;
    setImage(next);
  }

  async function pickAnimal(animal) {
    setBusy(animal);
    setStatus(null);
    try {
      const { blob, sourceUrl } = await fetchAnimalImage(animal);
      const objectUrl = URL.createObjectURL(blob);
      show({
        src: objectUrl,
        objectUrl,
        blob,
        animal,
        sourceUrl,
        id: null,
        sizeBytes: blob.size,
        createdAt: null,
      });
      setStatus(null);
    } catch (error) {
      setStatus({ kind: 'error', message: error.message });
    } finally {
      setBusy(null);
    }
  }

  async function saveCurrentImage() {
    if (!image?.blob) {
      return;
    }
    setBusy('save');
    setStatus(null);
    try {
      const stored = await saveImage(image);
      // Same picture, now with an identity in the backend — no new object URL involved.
      setImage((current) =>
        current ? { ...current, id: stored.id, createdAt: stored.createdAt } : current,
      );
      setStatus({ kind: 'success', message: `Saved to the backend as image #${stored.id}.` });
    } catch (error) {
      setStatus({ kind: 'error', message: error.message });
    } finally {
      setBusy(null);
    }
  }

  async function loadLastSavedImage() {
    setBusy('load');
    setStatus(null);
    try {
      const stored = await fetchLatestImage();
      if (!stored) {
        setStatus({ kind: 'info', message: 'The backend has no saved image yet.' });
        return;
      }
      show({
        src: contentUrl(stored.id),
        objectUrl: null,
        blob: null,
        animal: stored.animal,
        sourceUrl: stored.sourceUrl,
        id: stored.id,
        sizeBytes: stored.sizeBytes,
        createdAt: stored.createdAt,
      });
      setStatus({ kind: 'success', message: `Loaded image #${stored.id} from the backend.` });
    } catch (error) {
      setStatus({ kind: 'error', message: error.message });
    } finally {
      setBusy(null);
    }
  }

  const fetchingAnimal = ANIMAL_IDS.includes(busy) ? busy : null;
  const canSave = Boolean(image?.blob) && !image.id && busy === null;

  return (
    <main className="app">
      <header>
        <h1>Save the Animals</h1>
        <p className="subtitle">
          Fetch a random animal through the proxy, keep the one you like in SQLite.
        </p>
      </header>

      <section>
        <h2>Backend</h2>
        <HealthButton />
      </section>

      <section>
        <h2>Fetch a picture</h2>
        <AnimalButtons onPick={pickAnimal} busyAnimal={fetchingAnimal} disabled={busy !== null} />
      </section>

      <section>
        <h2>Picture</h2>
        <ImagePanel image={image} loading={fetchingAnimal !== null} />
        <div className="control-row">
          <button type="button" onClick={saveCurrentImage} disabled={!canSave}>
            {busy === 'save' ? 'Saving…' : 'Save this picture'}
          </button>
          <button type="button" onClick={loadLastSavedImage} disabled={busy !== null}>
            {busy === 'load' ? 'Loading…' : 'Load last saved picture'}
          </button>
        </div>
        <StatusBanner status={status} />
      </section>
    </main>
  );
}

import { useState } from 'react';

import { checkHealth } from '../api/client.js';

const LABELS = {
  unknown: 'Not checked yet',
  checking: 'Checking…',
  up: 'UP',
  down: 'DOWN',
};

export default function HealthButton() {
  const [health, setHealth] = useState({ kind: 'unknown' });

  async function check() {
    setHealth({ kind: 'checking' });
    try {
      const report = await checkHealth();
      setHealth({
        kind: report.up ? 'up' : 'down',
        detail: report.database ? `database ${report.database}` : report.status,
      });
    } catch (error) {
      // A network-level failure means the backend is not reachable at all.
      setHealth({ kind: 'down', detail: error.message });
    }
  }

  return (
    <div className="control-row">
      <button type="button" onClick={check} disabled={health.kind === 'checking'}>
        Check backend health
      </button>
      <span className={`pill pill--${health.kind}`} data-testid="health-status">
        {LABELS[health.kind]}
        {health.detail ? ` — ${health.detail}` : ''}
      </span>
    </div>
  );
}

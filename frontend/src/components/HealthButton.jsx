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

  // The lens is the button; the three lamps beside it report what came back.
  return (
    <div className="dexlamps">
      <button
        type="button"
        className="dexlamps__lens"
        data-state={health.kind}
        onClick={check}
        disabled={health.kind === 'checking'}
        title="Check backend health"
        aria-label="Check backend health"
      />
      <span className="dexlamps__row" aria-hidden="true">
        <i className="lamp lamp--red" data-lit={health.kind === 'down'} />
        <i className="lamp lamp--amber" data-lit={health.kind === 'checking'} />
        <i className="lamp lamp--green" data-lit={health.kind === 'up'} />
      </span>
      <span className={`pill pill--${health.kind}`} data-testid="health-status">
        {LABELS[health.kind]}
        {health.detail ? ` — ${health.detail}` : ''}
      </span>
    </div>
  );
}

import { ANIMALS } from '../api/animals.js';

export default function AnimalButtons({ onPick, busyAnimal, disabled }) {
  return (
    <div className="control-row">
      {ANIMALS.map(({ id, label, service }) => (
        <button
          key={id}
          type="button"
          onClick={() => onPick(id)}
          disabled={disabled}
          title={`Served by ${service}`}
        >
          {busyAnimal === id ? 'Fetching…' : label}
        </button>
      ))}
    </div>
  );
}

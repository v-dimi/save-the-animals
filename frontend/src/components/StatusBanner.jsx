export default function StatusBanner({ status }) {
  if (!status) {
    return null;
  }
  return (
    <p
      className={`banner banner--${status.kind}`}
      role={status.kind === 'error' ? 'alert' : 'status'}
    >
      {status.message}
    </p>
  );
}

/** Regex matching an element whose visible text is exactly `value` (ignoring surrounding whitespace). */
export function exactly(value: string): RegExp {
  const escaped = value.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
  return new RegExp(`^\\s*${escaped}\\s*$`);
}

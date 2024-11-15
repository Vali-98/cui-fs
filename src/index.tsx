const CuiFs = require('./NativeCuiFs').default;

export function multiply(a: number, b: number): number {
  return CuiFs.multiply(a, b);
}

import type { FileOptionsT } from './NativeCuiFs';
import CuiFs from './NativeCuiFs';

export async function writeFile(
  path: string,
  b64: string,
  options: FileOptionsT
): Promise<void> {
  return CuiFs.writeFile(path, b64, options);
}

export function copyFileRes(from: string, into: string): Promise<void> {
  return CuiFs.copyFileRes(from, normalizeFilePath(into));
}

function normalizeFilePath(path: string): string {
  return path.startsWith('file://') ? path.slice(7) : path;
}

export function copyFile(
  from: string,
  into: string,
  options: FileOptionsT = {}
): Promise<void> {
  return CuiFs.copyFile(
    normalizeFilePath(from),
    normalizeFilePath(into),
    options
  );
}

export const {
  MainBundlePath,
  CachesDirectoryPath,
  ExternalCachesDirectoryPath,
  DocumentDirectoryPath,
  DownloadDirectoryPath,
  ExternalDirectoryPath,
  ExternalStorageDirectoryPath,
  TemporaryDirectoryPath,
  LibraryDirectoryPath,
  PicturesDirectoryPath, // For Windows
  FileProtectionKeys,
  RoamingDirectoryPath, // For Windows
} = CuiFs.getConstants();

import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

export type FileOptionsT = {
  // iOS-specific.
  NSFileProtectionKey?: string;
};

export interface Spec extends TurboModule {
  readonly getConstants: () => {
    // System paths.
    CachesDirectoryPath: string;
    DocumentDirectoryPath: string;
    DownloadDirectoryPath: string;
    ExternalCachesDirectoryPath: string;
    ExternalDirectoryPath: string;
    ExternalStorageDirectoryPath: string;
    MainBundlePath?: string; // not on Android
    TemporaryDirectoryPath: string;

    // File system entity types.
    // TODO: At least on iOS there more file types we don't capture here:
    // https://developer.apple.com/documentation/foundation/nsfileattributetype?language=objc
    FileTypeRegular: string;
    FileTypeDirectory: string;

    // TODO: It was not declared in JS layer,
    // but it is exported constant on Android. Do we need it?
    DocumentDirectory: number;

    // iOS-specific
    LibraryDirectoryPath?: string;

    // Windows-specific.
    PicturesDirectoryPath?: string; // also on Android!
    RoamingDirectoryPath?: string;

    // NON-ANDROID-STUFF, AND NOT DOCUMENTED
    FileProtectionKeys?: string;
  };

  copyFileRes(from: string, into: string): Promise<void>;
  writeFile(path: string, b64: string, options: FileOptionsT): Promise<void>;
  copyFile(from: string, into: string, options: FileOptionsT): Promise<void>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('CuiFs');

# cui-fs

Custom filesystem functions for ChatterUI based on [react-native-fs](https://github.com/birdofpreyru/react-native-fs/tree/master)

This library is a subset of react-native-fs but with all unused functions and dependencies removed for maintainability purposes.

Currently Android only.

## Installation

```sh
npm install cui-fs
```

## Usage

This package has two specific functions which are lacking from expo-filesystem, thus prompting this module creation:

- copyFileRes - copies a file from app resources
- writeFile - this function is specifically used for copying app files to default Android downloads directory

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)

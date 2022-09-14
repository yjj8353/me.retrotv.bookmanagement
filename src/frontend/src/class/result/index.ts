type Key = string | number;
type Value = string | number | object | boolean | undefined | null;

export class Result implements ResultType {
  result: Map<Key, Value>;

  constructor() {
    this.result = new Map<Key, Value>();
  }

  add(key: Key, value: Value) {
    this.result.set(key, value);
  }

  delete(key: Key) {
    this.result.delete(key);
  }

  get(key: Key) {
    return this.result.get(key);
  }
}

export interface ResultType {
  result: Map<Key, Value>;
}

export enum KeyType {
  SUCCESS = 'success',
  MESSAGE = 'message',
  DATA = 'data',
}

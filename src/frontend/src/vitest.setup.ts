const fakeStorage = (() => {
  let store: Record<string, string> = {};
  return {
    getItem: (key: string) => store[key] ?? null,
    setItem: (key: string, value: string) => { store[key] = value; },
    removeItem: (key: string) => { delete store[key]; },
    clear: () => { store = {}; },
    get length() { return Object.keys(store).length; },
    key: (_: number) => null,
  } as Storage;
})();

Object.defineProperty(globalThis, 'localStorage', { value: fakeStorage, writable: true, configurable: true });

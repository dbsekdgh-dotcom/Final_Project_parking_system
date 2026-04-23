import { create } from 'zustand';

const useStoreStore = create((set) =>({
    storeId:null,
    storeName: "",
    token: null,
    wallets: [],

    setStoreInfo: (storeId, storeName, token) =>
        set({ storeId, storeName, token }),
    setWallets: (wallets) => set({ wallets }),
    clearStore: ()=> set({storeId: null, storeName: '', token: null, wallets: []})

}));

export default useStoreStore;
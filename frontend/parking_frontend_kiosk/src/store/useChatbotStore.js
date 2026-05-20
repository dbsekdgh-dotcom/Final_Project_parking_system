import { create } from 'zustand';

const useChatbotStore = create(set => ({
    screenId: 'unknown',
    setScreenId: (screenId) => set({ screenId }),
    open: false,
    setOpen: (open) => set({ open }),
    toggleOpen: () => set(state => ({ open: !state.open })),
}));

export default useChatbotStore;

import { create } from 'zustand';

const useChatbotStore = create(set => ({
    screenId: 'unknown',
    setScreenId: (screenId) => set({ screenId }),
}));

export default useChatbotStore;

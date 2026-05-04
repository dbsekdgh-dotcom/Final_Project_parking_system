import { createContext, useContext } from 'react'

export const PendingCountContext = createContext(() => {})

export const usePendingCountRefresh = () => useContext(PendingCountContext)

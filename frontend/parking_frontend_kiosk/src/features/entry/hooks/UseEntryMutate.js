import { useMutation } from "@tanstack/react-query";
import { createEntry } from "../api/EntryApi"
export const useEntryMutation = () => {
  return useMutation({
    mutationFn: createEntry,
  });
};
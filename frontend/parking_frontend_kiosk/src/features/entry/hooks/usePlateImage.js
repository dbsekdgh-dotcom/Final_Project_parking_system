import { useMutation } from "@tanstack/react-query";
import { createEntry } from "../api/EntryApi";

export const usePlateOCRMutation = () =>{
    return useMutation({
        mutationFn: createEntry,
    });
};
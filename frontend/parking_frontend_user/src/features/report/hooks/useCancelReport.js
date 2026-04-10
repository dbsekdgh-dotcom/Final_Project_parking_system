import { useMutation, useQueryClient } from "@tanstack/react-query";
import { cancelReport } from "../api/ReportApi";

export const useCancelReport = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: cancelReport,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["reports"] });
    },
  });
};

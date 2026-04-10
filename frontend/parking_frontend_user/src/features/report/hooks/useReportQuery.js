import { useQuery } from "@tanstack/react-query";
import { fetchMyReports, fetchReceivedReports } from "../api/ReportApi";

export const useMyReports = (page = 0) => {
  return useQuery({
    queryKey: ["reports", "my", page],
    queryFn: () => fetchMyReports(page),
  });
};

export const useReceivedReports = (page = 0) => {
  return useQuery({
    queryKey: ["reports", "received", page],
    queryFn: () => fetchReceivedReports(page),
  });
};

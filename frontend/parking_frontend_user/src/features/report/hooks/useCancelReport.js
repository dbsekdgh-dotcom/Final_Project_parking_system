import { useMutation, useQueryClient } from "@tanstack/react-query";
import { cancelReport} from "../api/ReportApi";

export const useCancelReport = () => {
  const queryClient = useQueryClient();

  return useMutation({

    //함수를 mutationFn이라는 이름으로 명확하게 지정!
    mutationFn: (reportId)=> cancelReport(reportId),  
    onSuccess: () => {

      //내가신고한 내역을 불러오는 쿼리 키를 넣어서 데이터를 다시 불러오게 해야됨
      queryClient.invalidateQueries(["myReports"]); 
      queryClient.invalidateQueries(["receivedReports"]);
      alert("신고가 취소되었습니다.");
    },
      onError: (error) => {
        console.error("취소 실패:",error);
        alert("취소 둥 오류가 발생하였습니다.");
    }
  });
};

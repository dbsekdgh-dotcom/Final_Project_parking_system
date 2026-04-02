//package com.example.demo.domain.user.vehicle.service;
//
//import com.example.demo.domain.shared.user.User;
//import com.example.demo.domain.shared.user.UserRepository;
//import com.example.demo.domain.shared.vehicle.Vehicle;
//import com.example.demo.domain.shared.vehicle.enums.VehicleStatus;
//
//import com.example.demo.domain.shared.vehicle.VehicleRepository;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.File;
//import java.io.IOException;
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class VehicleService {
//
//    private final VehicleRepository vehicleRepository;
//    private final UserRepository userRepository;
//    private final OcrService ocrService; //OCR처리용 서비스
//
//    /* 차량등록
//       1. 회원 존재 여부 확인
//       2. 차량등록증 OCR로 차량 번호 추출
//       3. 파일 저장
//       4. Vehicle엔티티 생성 후 DB 저장
//     */
//    @Transactional //DB쓰기 작업이 있으므로 트랙잭션 권장
//    public Vehicle registerVehicle(Long memberId, String vehicleName, MultipartFile idCard, MultipartFile vehicleReg)throws Exception{
//
//        //1. 회원 조회
//        User member = userRepository.findById(memberId)
//                .orElseThrow(()->new IllegalArgumentException("회원이 존재하지 않습니다."));
//
//        //2. OCR처리 (차량 등록증에서 차량번호 추출)
//        String vehicleRegText = ocrService.extractText(vehicleReg);
//        String carNumber = ocrService.parseVehicleReg(vehicleRegText).get("vehicleNumber");
//
//        //3. 파일 저장(예외처리 강화)
//        String idCardPath = saveFile(idCard);
//        String vehicleRegPath = saveFile(vehicleReg);
//
//        //4. Vehicle 엔티티 생성 + DB 저장
////        return vehicleRepository.save(Vehicle.builder()
////                .user(member) //회원과 연동
////                .vehicleName(vehicleName)
////                .carNumber(carNumber)
////                .idCardPath(idCardPath)
////                .vehicleRegPath(vehicleRegPath)
////                .status(VehicleStatus.ACTIVE)
////                .build());
////    }
//    /*
//       소프트 삭제
//       status를 DELETED로 변경하고 deleted_at 기록
//     */
//    public void deleteVehicle(Long vehicleId){
//        VehicleEntity vehicle = vehicleRepository.findById(vehicleId)
//                .orElseThrow(()-> new IllegalArgumentException("차량이 존재하지 않습니다."));
//
//        vehicle.setStatus(VehicleStatus.DELETED);
//        vehicle.setDeletedAt(LocalDateTime.now());
//        vehicleRepository.save(vehicle);
//    }
//    /* 특정 회원의 활성 차량 조회
//     */
//    public List<VehicleEntity> findActiveVehiclesByMember(Long memberId){
//        return vehicleRepository.findByUserUserIdAndStatus(memberId,VehicleStatus.ACTIVE);
//    }
//    /* 파일 저장 메서드
//     */
//    private String saveFile(MultipartFile file) throws IOException{
//        String path ="c:/upload/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
//        file.transferTo(new File(path));
//        return path;
//        //윈도우 환경에서는 문제 없지만, 서버 배포 환경에서는 환경 변수 또는 application.properties로 경로 관리 권장
//        //폴더 존재 여부 확인 필요 (c:/upload/ 없으면 IOException 발생)
//    }
//}
////registerVehicle
////-> 회원 존재 확인
////-> OCR로 차량 번호 추출
////-> 파일 저장 + 엔티티 생성
////-> DB 저장
////deleteVehicle-> soft delete: 실제 삭제 X, status = DELETED + deletedAt 기록
////findActiveVehiclesByMember-> 회원 기준으로 활성 차량만 조회
////saveFile-> 업로드 파일을 서버 디스크에 저장하고 경로 반환
-- MySQL dump 10.13  Distrib 8.0.46, for Linux (x86_64)
--
-- Host: localhost    Database: parking
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `activity_log`
--

DROP TABLE IF EXISTS `activity_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `activity_log` (
  `activity_id` bigint NOT NULL AUTO_INCREMENT,
  `activity_type` enum('ENTRY','EXIT','PAYMENT_PRE','PAYMENT_EXIT','PAYMENT_CANCEL','RESERVATION_CREATED','RESERVATION_CANCELLED','VEHICLE_REGISTERED','RESIDENT_REGISTERED','PASS_PURCHASED','COUPON_PURCHASED','COUPON_USED','ADMIN_FORCE_EXIT','REFUNDED') NOT NULL,
  `parking_log_id` bigint DEFAULT NULL,
  `reservation_id` bigint DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  `payment_id` bigint DEFAULT NULL,
  `car_number` varchar(255) DEFAULT NULL,
  `household_id` bigint DEFAULT NULL,
  `message` varchar(255) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`activity_id`),
  KEY `fk_activity_reservation` (`reservation_id`),
  KEY `fk_activity_payment` (`payment_id`),
  KEY `idx_activity_time` (`created_at`),
  KEY `idx_activity_household` (`household_id`),
  KEY `idx_activity_parking_log` (`parking_log_id`),
  KEY `idx_act_user_id` (`user_id`),
  KEY `idx_act_car_type_time` (`car_number`,`activity_type`,`created_at`),
  CONSTRAINT `fk_activity_household` FOREIGN KEY (`household_id`) REFERENCES `household` (`household_id`) ON DELETE SET NULL,
  CONSTRAINT `fk_activity_parking_log` FOREIGN KEY (`parking_log_id`) REFERENCES `parking_log` (`parking_log_id`),
  CONSTRAINT `fk_activity_payment` FOREIGN KEY (`payment_id`) REFERENCES `payment` (`payment_id`),
  CONSTRAINT `fk_activity_reservation` FOREIGN KEY (`reservation_id`) REFERENCES `reservation` (`reservation_id`),
  CONSTRAINT `fk_activity_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=87 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `activity_log`
--

LOCK TABLES `activity_log` WRITE;
/*!40000 ALTER TABLE `activity_log` DISABLE KEYS */;
INSERT INTO `activity_log` VALUES (1,'ENTRY',1,NULL,NULL,NULL,'356가7890',NULL,'입차 완료','2026-05-06 00:21:24'),(2,'EXIT',1,NULL,NULL,NULL,'356가7890',NULL,'출차 완료','2026-05-06 00:25:13'),(3,'ENTRY',2,NULL,NULL,NULL,'65노0887',NULL,'입차 완료','2026-05-06 00:32:43'),(4,'ENTRY',3,NULL,NULL,NULL,'02로0202',NULL,'입차 완료','2026-05-06 00:34:03'),(5,'ENTRY',4,NULL,NULL,NULL,'57서9757',NULL,'입차 완료','2026-05-06 00:34:28'),(6,'ENTRY',5,NULL,NULL,NULL,'69도3445',NULL,'입차 완료','2026-05-06 00:35:50'),(7,'RESIDENT_REGISTERED',NULL,NULL,3,NULL,NULL,2,'입주민 신청 접수 : 102호 (신청자: 윤상호)','2026-05-06 00:37:43'),(8,'RESIDENT_REGISTERED',NULL,NULL,4,NULL,NULL,20,'입주민 신청 접수 : 120호 (신청자: 김보경)','2026-05-06 00:38:00'),(9,'VEHICLE_REGISTERED',NULL,NULL,3,NULL,'123가4568',NULL,'차량 등록: 123가4568 (유사도-유저: 0.0%, 서류: 100.0%, 상태: PENDING, 무수정: false)','2026-05-06 00:39:14'),(10,'RESERVATION_CREATED',NULL,1,NULL,NULL,'154러7070',2,'[154러7070] 차량 방문 예약 신청','2026-05-06 00:43:22'),(11,'RESIDENT_REGISTERED',NULL,NULL,6,NULL,NULL,1,'입주민 신청 접수 : 101호 (신청자: 유승원)','2026-05-06 00:57:47'),(12,'VEHICLE_REGISTERED',NULL,NULL,6,NULL,'12거3456',NULL,'차량 등록: 12거3456 (유사도-유저: 0.0%, 서류: 100.0%, 상태: PENDING, 무수정: false)','2026-05-06 00:58:35'),(13,'PAYMENT_PRE',2,NULL,NULL,2,'65노0887',NULL,'PAYMENT_PRE완료','2026-05-06 00:59:16'),(14,'PAYMENT_PRE',3,NULL,NULL,3,'02로0202',NULL,'PAYMENT_PRE완료','2026-05-06 01:02:42'),(15,'VEHICLE_REGISTERED',NULL,NULL,5,NULL,'69도3445',NULL,'차량 등록: 69도3445 (유사도-유저: 0.0%, 서류: 100.0%, 상태: PENDING, 무수정: false)','2026-05-06 01:10:32'),(16,'VEHICLE_REGISTERED',NULL,NULL,7,NULL,'02로0202',NULL,'차량 등록: 02로0202 (유사도-유저: 0.0%, 서류: 100.0%, 상태: PENDING, 무수정: false)','2026-05-06 01:10:34'),(17,'PASS_PURCHASED',NULL,NULL,7,4,'02로0202',NULL,'정기권 구매 완료','2026-05-06 01:11:22'),(18,'PAYMENT_PRE',5,NULL,NULL,5,'69도3445',NULL,'PAYMENT_PRE완료','2026-05-06 01:19:14'),(19,'EXIT',5,NULL,NULL,NULL,'69도3445',NULL,'출차 완료','2026-05-06 01:19:16'),(20,'RESIDENT_REGISTERED',NULL,NULL,5,NULL,NULL,3,'입주민 신청 접수 : 103호 (신청자: 이윤진)','2026-05-06 01:19:46'),(21,'RESERVATION_CREATED',NULL,2,NULL,NULL,'000가0000',3,'[000가0000] 차량 방문 예약 신청','2026-05-06 01:20:30'),(22,'ENTRY',6,NULL,NULL,NULL,'69도3445',3,'입차 완료','2026-05-06 01:21:15'),(23,'RESERVATION_CREATED',NULL,3,NULL,NULL,'65라4088',3,'[65라4088] 차량 방문 예약 신청','2026-05-06 01:29:11'),(24,'ENTRY',7,NULL,NULL,NULL,'65라4088',NULL,'입차 완료','2026-05-06 01:29:54'),(25,'RESERVATION_CREATED',NULL,4,NULL,NULL,'11나1234',1,'[11나1234] 차량 방문 예약 신청','2026-05-06 01:31:36'),(26,'EXIT',6,NULL,NULL,NULL,'69도3445',3,'출차 완료','2026-05-06 01:40:09'),(27,'ENTRY',9,NULL,NULL,NULL,'69도3445',3,'입차 완료','2026-05-06 02:11:20'),(28,'ENTRY',10,NULL,NULL,NULL,'391서8288',NULL,'입차 완료','2026-05-06 03:09:48'),(29,'RESIDENT_REGISTERED',NULL,NULL,6,NULL,NULL,1,'입주민 신청 접수 : 101호 (신청자: 유승원)','2026-05-06 03:53:22'),(30,'RESERVATION_CREATED',NULL,5,NULL,NULL,'12구1234',1,'[12구1234] 차량 방문 예약 신청','2026-05-06 03:55:47'),(31,'RESIDENT_REGISTERED',NULL,NULL,6,NULL,NULL,1,'입주민 신청 접수 : 101호 (신청자: 유승원)','2026-05-06 03:57:46'),(32,'PAYMENT_PRE',2,NULL,NULL,10,'65노0887',NULL,'PAYMENT_PRE완료','2026-05-06 03:59:54'),(33,'EXIT',2,NULL,NULL,NULL,'65노0887',NULL,'출차 완료','2026-05-06 03:59:56'),(34,'PASS_PURCHASED',NULL,NULL,6,11,'12거3456',NULL,'정기권 구매 완료','2026-05-06 04:01:40'),(35,'PAYMENT_PRE',7,NULL,NULL,12,'65라4088',NULL,'PAYMENT_PRE완료','2026-05-06 04:09:42'),(36,'EXIT',7,NULL,NULL,NULL,'65라4088',NULL,'출차 완료','2026-05-06 04:09:44'),(37,'PAYMENT_PRE',10,NULL,NULL,15,'391서8288',NULL,'PAYMENT_PRE완료','2026-05-06 04:14:24'),(38,'EXIT',9,NULL,NULL,NULL,'69도3445',3,'출차 완료','2026-05-06 04:17:10'),(39,'ENTRY',15,NULL,NULL,NULL,'69도3445',3,'입차 완료','2026-05-06 04:17:39'),(40,'EXIT',15,NULL,NULL,NULL,'69도3445',3,'출차 완료','2026-05-06 04:18:00'),(41,'PAYMENT_PRE',10,NULL,NULL,20,'391서8288',NULL,'PAYMENT_PRE완료','2026-05-06 05:13:25'),(42,'REFUNDED',NULL,NULL,6,11,'12거3456',NULL,'정기권 환불 처리','2026-05-06 05:14:16'),(43,'ENTRY',16,NULL,NULL,NULL,'137로2805',NULL,'입차 완료','2026-05-06 05:14:43'),(44,'ENTRY',17,NULL,NULL,NULL,'65라4088',NULL,'입차 완료','2026-05-06 05:17:12'),(45,'ENTRY',20,NULL,NULL,NULL,'145보6946',NULL,'입차 완료','2026-05-06 05:33:32'),(46,'ENTRY',21,NULL,NULL,NULL,'123가4568',2,'입차 완료','2026-05-06 05:33:43'),(47,'EXIT',21,NULL,NULL,NULL,'123가4568',2,'출차 완료','2026-05-06 05:34:07'),(48,'PAYMENT_PRE',3,NULL,NULL,22,'02로0202',NULL,'PAYMENT_PRE완료','2026-05-06 05:35:35'),(49,'EXIT',3,NULL,NULL,NULL,'02로0202',NULL,'출차 완료','2026-05-06 05:35:57'),(50,'ENTRY',22,NULL,NULL,NULL,'123가4568',2,'입차 완료','2026-05-06 05:36:18'),(51,'EXIT',22,NULL,NULL,NULL,'123가4568',2,'출차 완료','2026-05-06 05:37:44'),(52,'PAYMENT_PRE',16,NULL,NULL,23,'137로2805',NULL,'PAYMENT_PRE완료','2026-05-06 05:37:57'),(53,'ENTRY',23,NULL,NULL,NULL,'123가4568',2,'입차 완료','2026-05-06 05:38:05'),(54,'ADMIN_FORCE_EXIT',23,NULL,NULL,NULL,'123가4568',NULL,'관리자 강제출차:관리자 직접 조치 - 테스트중','2026-05-06 05:43:29'),(55,'ENTRY',24,NULL,NULL,NULL,'123가4568',2,'입차 완료','2026-05-06 05:49:17'),(56,'EXIT',24,NULL,NULL,NULL,'123가4568',2,'출차 완료','2026-05-06 05:50:31'),(57,'VEHICLE_REGISTERED',NULL,NULL,9,NULL,'12거3456',NULL,'차량 등록: 12거3456 (유사도-유저: 100.0%, 서류: 100.0%, 상태: ACTIVE, 무수정: true)','2026-05-06 05:57:31'),(58,'VEHICLE_REGISTERED',NULL,NULL,9,NULL,'12거3456',NULL,'차량 등록: 12거3456 (유사도-유저: 100.0%, 서류: 100.0%, 상태: PENDING, 무수정: false)','2026-05-06 05:57:57'),(59,'VEHICLE_REGISTERED',NULL,NULL,9,NULL,'12거3456',NULL,'차량 등록: 12거3456 (유사도-유저: 100.0%, 서류: 100.0%, 상태: ACTIVE, 무수정: true)','2026-05-06 05:58:13'),(60,'PASS_PURCHASED',NULL,NULL,9,25,'12거3456',NULL,'정기권 구매 완료','2026-05-06 05:58:33'),(61,'REFUNDED',NULL,NULL,9,25,'12거3456',NULL,'정기권 환불 처리','2026-05-06 05:58:47'),(62,'RESIDENT_REGISTERED',NULL,NULL,9,NULL,NULL,1,'입주민 신청 접수 : 101호 (신청자: 홍길동)','2026-05-06 05:58:58'),(63,'RESERVATION_CREATED',NULL,6,NULL,NULL,'11가1234',1,'[11가1234] 차량 방문 예약 신청','2026-05-06 05:59:15'),(64,'RESERVATION_CREATED',NULL,7,NULL,NULL,'11가1234',1,'[11가1234] 차량 방문 예약 신청','2026-05-06 06:00:44'),(65,'RESIDENT_REGISTERED',NULL,NULL,9,NULL,NULL,1,'입주민 신청 접수 : 101호 (신청자: 홍길동)','2026-05-06 06:01:15'),(66,'ENTRY',26,NULL,NULL,NULL,'69도3445',3,'입차 완료','2026-05-06 06:06:52'),(67,'EXIT',26,NULL,NULL,NULL,'69도3445',3,'출차 완료','2026-05-06 06:07:14'),(68,'ENTRY',27,NULL,NULL,NULL,'02로0202',NULL,'입차 완료','2026-05-06 06:20:19'),(69,'ADMIN_FORCE_EXIT',27,NULL,NULL,NULL,'02로0202',NULL,'관리자 강제출차:관리자 직접 조치','2026-05-08 05:00:45'),(70,'ENTRY',28,NULL,NULL,NULL,'02로0202',NULL,'입차 완료','2026-05-08 06:41:40'),(71,'RESERVATION_CREATED',NULL,8,NULL,NULL,'12가4568',20,'[12가4568] 차량 방문 예약 신청','2026-05-10 08:06:26'),(72,'ENTRY',30,NULL,NULL,NULL,'157고4895',NULL,'입차 완료','2026-05-11 05:15:16'),(73,'ENTRY',31,NULL,NULL,NULL,'69도3445',3,'입차 완료','2026-05-13 08:24:42'),(74,'PAYMENT_PRE',30,NULL,NULL,26,'157고4895',NULL,'PAYMENT_PRE완료','2026-05-14 06:57:10'),(75,'EXIT',28,NULL,NULL,NULL,'02로0202',NULL,'출차 완료','2026-05-14 08:04:08'),(76,'REFUNDED',NULL,NULL,7,4,'02로0202',NULL,'정기권 환불 처리','2026-05-14 08:04:13'),(77,'ENTRY',33,NULL,NULL,NULL,'02로0202',NULL,'입차 완료','2026-05-14 08:04:48'),(78,'PAYMENT_PRE',33,NULL,NULL,27,'02로0202',NULL,'PAYMENT_PRE완료','2026-05-15 16:01:38'),(79,'PAYMENT_PRE',33,NULL,NULL,28,'02로0202',NULL,'PAYMENT_PRE완료','2026-05-15 16:01:38'),(80,'ENTRY',34,NULL,3,NULL,'123가4568',2,'입차 완료','2026-05-19 11:46:08'),(81,'EXIT',34,NULL,3,NULL,'123가4568',2,'출차 완료','2026-05-19 11:46:53'),(82,'PAYMENT_PRE',33,NULL,NULL,29,'02로0202',NULL,'PAYMENT_PRE완료','2026-05-20 15:24:52'),(83,'PAYMENT_PRE',30,NULL,NULL,30,'157고4895',NULL,'PAYMENT_PRE완료','2026-05-20 15:25:41'),(84,'PAYMENT_PRE',33,NULL,NULL,31,'02로0202',NULL,'PAYMENT_PRE완료','2026-05-20 16:22:14'),(85,'PAYMENT_PRE',30,NULL,NULL,32,'157고4895',NULL,'PAYMENT_PRE완료','2026-05-20 16:22:43'),(86,'PAYMENT_PRE',30,NULL,NULL,33,'157고4895',NULL,'PAYMENT_PRE완료','2026-05-20 17:21:28');
/*!40000 ALTER TABLE `activity_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `admin`
--

DROP TABLE IF EXISTS `admin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin` (
  `admin_id` bigint NOT NULL AUTO_INCREMENT,
  `login_id` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `name` varchar(100) NOT NULL,
  `status` enum('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_login_at` datetime DEFAULT NULL,
  `deleted_at` datetime DEFAULT NULL,
  PRIMARY KEY (`admin_id`),
  UNIQUE KEY `login_id` (`login_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin`
--

LOCK TABLES `admin` WRITE;
/*!40000 ALTER TABLE `admin` DISABLE KEYS */;
INSERT INTO `admin` VALUES (1,'admin01','$2b$10$wMi1vLBRCPuyqqiNleEHru9jliBKBxTZATHC7c62xBH2RbB9u/p0i','윤상호','ACTIVE','2026-05-05 07:45:06','2026-05-20 16:15:44',NULL),(2,'admin02','$2b$10$LqvaM0IwX5zBT7hqDim5t.LYky4bJcr7aorkNVrXo08..ZqqyZV1e','김보경','ACTIVE','2026-05-05 07:45:06','2026-05-26 15:57:46',NULL),(3,'admin03','$2b$10$L1UDD5O2.7oqjhLu7yP63elnvEp.bNBJbLKQDXwIECfjI9wqPanzS','최주연','ACTIVE','2026-05-05 07:45:06','2026-06-11 11:49:13',NULL),(4,'admin04','$2b$10$MDwqScOtuR13VnMQYrnlDeFRi6eMq3FjHEtLFzbKTIC7WxkJYzgT2','유승원','ACTIVE','2026-05-05 07:45:06','2026-05-06 10:29:16',NULL),(5,'admin05','$2b$10$pkkbYoPy/x4FDFRdfkK2mOli7SyYKaJ/kr6V.wSOmk/OaWLKVGhS2','이윤진','ACTIVE','2026-05-05 07:45:06','2026-05-21 11:30:52',NULL),(6,'test01','$2b$10$aJ6ka43NgvU0d.3R4HfUuOn.O1UD6cx9WsEjsrWkWG3YotKaFmFWK','test01','ACTIVE','2026-05-11 06:58:59','2026-05-21 11:49:33',NULL),(7,'test02','$2b$10$uAYkm7PPKqeAX7X0oLmpq.qAXC8et5CuVDzSs7XDnEoYcMcerCXja','test02','ACTIVE','2026-05-11 06:58:59','2026-05-11 18:12:25',NULL),(8,'test03','$2b$10$5FV9dhtHaTbr09Eu08q/3Ox2ua2XyfHQmMOmL6mRH0zJwsJTULNo2','test03','ACTIVE','2026-05-11 06:58:59','2026-05-18 18:01:23',NULL);
/*!40000 ALTER TABLE `admin` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `admin_action_log`
--

DROP TABLE IF EXISTS `admin_action_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin_action_log` (
  `action_id` bigint NOT NULL AUTO_INCREMENT COMMENT '로그 고유 번호',
  `admin_id` bigint NOT NULL COMMENT '행위를 수행한 관리자 ID',
  `target_type` enum('USER','VEHICLE','PAYMENT','POLICY','RESERVATION','SYSTEM_SETTING','STORE','PARKING_LOG','PARKING_SPACE','REPORT','RESERVATION_POLICY') NOT NULL COMMENT '대상 도메인',
  `action_type` enum('CREATE','UPDATE','DELETE','APPROVE','REJECT','REFUND','REPORT','BLACKLIST','ACTIVE','INACTIVE') NOT NULL COMMENT '수행 작업 유형',
  `target_id` bigint DEFAULT NULL,
  `before_data` json NOT NULL,
  `after_data` json NOT NULL,
  `is_reverted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '되돌림 여부',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `reverted_by_admin_id` bigint DEFAULT NULL COMMENT '원복을 수행한 관리자 ID',
  `reverted_at` datetime DEFAULT NULL,
  `changed_fields` json DEFAULT NULL COMMENT '실제 변경된 필드 목록 및 값',
  PRIMARY KEY (`action_id`),
  KEY `fk_aal_admin` (`admin_id`),
  KEY `fk_aal_revert_admin` (`reverted_by_admin_id`),
  KEY `idx_aal_target_action` (`target_type`,`action_type`),
  KEY `idx_aal_created_at` (`created_at`),
  CONSTRAINT `fk_aal_admin` FOREIGN KEY (`admin_id`) REFERENCES `admin` (`admin_id`),
  CONSTRAINT `fk_aal_revert_admin` FOREIGN KEY (`reverted_by_admin_id`) REFERENCES `admin` (`admin_id`)
) ENGINE=InnoDB AUTO_INCREMENT=77 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin_action_log`
--

LOCK TABLES `admin_action_log` WRITE;
/*!40000 ALTER TABLE `admin_action_log` DISABLE KEYS */;
INSERT INTO `admin_action_log` VALUES (1,2,'STORE','ACTIVE',1,'{\"name\": \"1층 카페\", \"status\": \"INACTIVE\", \"terminalPassword\": \"1234\"}','{\"name\": \"관리자\", \"status\": \"ACTIVE\", \"terminalPassword\": \"123456\"}',0,'2026-05-05 16:53:58',NULL,NULL,NULL),(2,3,'POLICY','CREATE',3,'{\"id\": 3, \"admin\": {\"name\": \"최주연\", \"status\": \"ACTIVE\", \"adminId\": 3, \"loginId\": \"admin03\", \"password\": \"$2b$10$L1UDD5O2.7oqjhLu7yP63elnvEp.bNBJbLKQDXwIECfjI9wqPanzS\", \"createdAt\": \"2026-05-05T07:45:06\", \"deletedAt\": null, \"lastLoginAt\": \"2026-05-05T21:19:34\"}, \"baseFee\": 500, \"unitFee\": 500, \"version\": 2, \"isActive\": false, \"createdAt\": \"2026-05-05T12:19:47.910099\", \"dailyMaxFee\": 10000, \"effectiveTo\": \"3000-01-01T00:00:00\", \"parkingType\": \"RESERVATION\", \"unitMinutes\": 10, \"graceMinutes\": 15, \"effectiveFrom\": \"2026-05-06T00:00:00\"}','{\"id\": 3, \"admin\": {\"name\": \"최주연\", \"status\": \"ACTIVE\", \"adminId\": 3, \"loginId\": \"admin03\", \"password\": \"$2b$10$L1UDD5O2.7oqjhLu7yP63elnvEp.bNBJbLKQDXwIECfjI9wqPanzS\", \"createdAt\": \"2026-05-05T07:45:06\", \"deletedAt\": null, \"lastLoginAt\": \"2026-05-05T21:19:34\"}, \"baseFee\": 500, \"unitFee\": 500, \"version\": 2, \"isActive\": false, \"createdAt\": \"2026-05-05T12:19:47.910099\", \"dailyMaxFee\": 10000, \"effectiveTo\": \"3000-01-01T00:00:00\", \"parkingType\": \"RESERVATION\", \"unitMinutes\": 10, \"graceMinutes\": 15, \"effectiveFrom\": \"2026-05-06T00:00:00\"}',0,'2026-05-05 12:19:48',NULL,NULL,NULL),(3,3,'POLICY','UPDATE',2,'{}','{}',0,'2026-05-05 12:19:48',NULL,NULL,NULL),(4,3,'POLICY','CREATE',5,'{\"name\": \"[상가] 2시간 할인권\", \"price\": 2000, \"status\": \"ACTIVE\", \"useType\": \"STORE\", \"createdAt\": \"2026-05-05T12:20:42.175793\", \"stackable\": true, \"validDays\": 30, \"freeTicket\": false, \"description\": \"상가 이용객 2시간 할인권 \", \"discountType\": \"TIME\", \"validMinutes\": 0, \"discountValue\": 120, \"ticketPolicyId\": 5}','{\"name\": \"[상가] 2시간 할인권\", \"price\": 2000, \"status\": \"ACTIVE\", \"useType\": \"STORE\", \"createdAt\": \"2026-05-05T12:20:42.175793\", \"stackable\": true, \"validDays\": 30, \"freeTicket\": false, \"description\": \"상가 이용객 2시간 할인권 \", \"discountType\": \"TIME\", \"validMinutes\": 0, \"discountValue\": 120, \"ticketPolicyId\": 5}',0,'2026-05-05 12:20:42',NULL,NULL,NULL),(5,3,'POLICY','CREATE',6,'{\"name\": \"[관리자] 2천원 할인권\", \"price\": 0, \"status\": \"ACTIVE\", \"useType\": \"ADMIN\", \"createdAt\": \"2026-05-05T12:21:40.127469\", \"stackable\": true, \"validDays\": 30, \"freeTicket\": true, \"description\": \"상가 무료지급 할인권\", \"discountType\": \"AMOUNT\", \"validMinutes\": 0, \"discountValue\": 2000, \"ticketPolicyId\": 6}','{\"name\": \"[관리자] 2천원 할인권\", \"price\": 0, \"status\": \"ACTIVE\", \"useType\": \"ADMIN\", \"createdAt\": \"2026-05-05T12:21:40.127469\", \"stackable\": true, \"validDays\": 30, \"freeTicket\": true, \"description\": \"상가 무료지급 할인권\", \"discountType\": \"AMOUNT\", \"validMinutes\": 0, \"discountValue\": 2000, \"ticketPolicyId\": 6}',0,'2026-05-05 12:21:40',NULL,NULL,NULL),(6,3,'POLICY','CREATE',7,'{\"name\": \"[관리자] 1천원 할인권\", \"price\": 0, \"status\": \"ACTIVE\", \"useType\": \"ADMIN\", \"createdAt\": \"2026-05-05T12:22:22.838545\", \"stackable\": true, \"validDays\": 30, \"freeTicket\": false, \"description\": \"관리자 조정: 1천원 할인\", \"discountType\": \"AMOUNT\", \"validMinutes\": 0, \"discountValue\": 1000, \"ticketPolicyId\": 7}','{\"name\": \"[관리자] 1천원 할인권\", \"price\": 0, \"status\": \"ACTIVE\", \"useType\": \"ADMIN\", \"createdAt\": \"2026-05-05T12:22:22.838545\", \"stackable\": true, \"validDays\": 30, \"freeTicket\": false, \"description\": \"관리자 조정: 1천원 할인\", \"discountType\": \"AMOUNT\", \"validMinutes\": 0, \"discountValue\": 1000, \"ticketPolicyId\": 7}',0,'2026-05-05 12:22:23',NULL,NULL,NULL),(7,3,'POLICY','CREATE',4,'{\"id\": 4, \"admin\": {\"name\": \"최주연\", \"status\": \"ACTIVE\", \"adminId\": 3, \"loginId\": \"admin03\", \"password\": \"$2b$10$L1UDD5O2.7oqjhLu7yP63elnvEp.bNBJbLKQDXwIECfjI9wqPanzS\", \"createdAt\": \"2026-05-05T07:45:06\", \"deletedAt\": null, \"lastLoginAt\": \"2026-05-05T21:19:34\"}, \"baseFee\": 1000, \"unitFee\": 700, \"version\": 2, \"isActive\": false, \"createdAt\": \"2026-05-05T12:23:38.994997\", \"dailyMaxFee\": 15000, \"effectiveTo\": \"3000-01-01T00:00:00\", \"parkingType\": \"VISIT\", \"unitMinutes\": 10, \"graceMinutes\": 10, \"effectiveFrom\": \"2026-05-06T00:00:00\"}','{\"id\": 4, \"admin\": {\"name\": \"최주연\", \"status\": \"ACTIVE\", \"adminId\": 3, \"loginId\": \"admin03\", \"password\": \"$2b$10$L1UDD5O2.7oqjhLu7yP63elnvEp.bNBJbLKQDXwIECfjI9wqPanzS\", \"createdAt\": \"2026-05-05T07:45:06\", \"deletedAt\": null, \"lastLoginAt\": \"2026-05-05T21:19:34\"}, \"baseFee\": 1000, \"unitFee\": 700, \"version\": 2, \"isActive\": false, \"createdAt\": \"2026-05-05T12:23:38.994997\", \"dailyMaxFee\": 15000, \"effectiveTo\": \"3000-01-01T00:00:00\", \"parkingType\": \"VISIT\", \"unitMinutes\": 10, \"graceMinutes\": 10, \"effectiveFrom\": \"2026-05-06T00:00:00\"}',0,'2026-05-05 12:23:39',NULL,NULL,NULL),(8,3,'POLICY','UPDATE',1,'{}','{}',0,'2026-05-05 12:23:39',NULL,NULL,NULL),(9,1,'PARKING_SPACE','UPDATE',1,'{\"status\": \"AVAILABLE\", \"disabled\": false, \"evCharge\": false, \"spaceCode\": \"B1-001\", \"isDisabled\": false, \"isEvCharge\": false}','{\"status\": \"AVAILABLE\", \"disabled\": false, \"evCharge\": true, \"spaceCode\": \"B1-001\", \"isDisabled\": false, \"isEvCharge\": true}',1,'2026-05-06 00:34:24',1,'2026-05-06 09:35:49',NULL),(10,1,'PARKING_SPACE','UPDATE',2,'{\"status\": \"AVAILABLE\", \"disabled\": false, \"evCharge\": false, \"spaceCode\": \"B1-002\", \"isDisabled\": false, \"isEvCharge\": false}','{\"status\": \"AVAILABLE\", \"disabled\": true, \"evCharge\": false, \"spaceCode\": \"B1-002\", \"isDisabled\": true, \"isEvCharge\": false}',0,'2026-05-06 00:34:27',NULL,NULL,NULL),(11,1,'USER','APPROVE',2,'{\"status\": \"PENDING\"}','{\"status\": \"APPROVED\"}',0,'2026-05-06 00:40:52',NULL,NULL,NULL),(12,1,'RESERVATION_POLICY','CREATE',1,'{\"id\": 1, \"eventName\": \"방문예약 정책\", \"startDate\": \"2026-05-06T09:41:00\", \"adminLoginId\": \"admin01\", \"policyStatus\": \"PERMANENT\", \"permittedMinutes\": 60, \"noShowPenaltyEnabled\": true, \"maxActiveReservations\": 1, \"dailyLimitPerHousehold\": 5, \"monthlyLimitPerHousehold\": 10}','{\"id\": 1, \"eventName\": \"방문예약 정책\", \"startDate\": \"2026-05-06T09:41:00\", \"adminLoginId\": \"admin01\", \"policyStatus\": \"PERMANENT\", \"permittedMinutes\": 60, \"noShowPenaltyEnabled\": true, \"maxActiveReservations\": 1, \"dailyLimitPerHousehold\": 5, \"monthlyLimitPerHousehold\": 10}',0,'2026-05-06 00:41:49',NULL,NULL,NULL),(13,1,'VEHICLE','APPROVE',3,'{\"status\": \"PENDING\"}','{\"status\": \"APPROVED\"}',0,'2026-05-06 00:42:43',NULL,NULL,NULL),(14,1,'USER','APPROVE',1,'{\"status\": \"PENDING\"}','{\"status\": \"APPROVED\"}',0,'2026-05-06 00:42:45',NULL,NULL,NULL),(15,1,'RESERVATION','APPROVE',4,'{\"status\": \"PENDING\"}','{\"status\": \"APPROVED\"}',0,'2026-05-06 00:44:04',NULL,NULL,NULL),(16,1,'STORE','ACTIVE',2,'{\"name\": \"1층 편의점\", \"status\": \"INACTIVE\", \"terminalPassword\": \"1234\"}','{\"name\": \"1층 편의점\", \"status\": \"ACTIVE\", \"terminalPassword\": \"456789\"}',0,'2026-05-06 00:49:44',NULL,NULL,NULL),(17,1,'VEHICLE','APPROVE',6,'{\"status\": \"PENDING\"}','{\"status\": \"APPROVED\"}',0,'2026-05-06 00:59:58',NULL,NULL,NULL),(18,1,'USER','APPROVE',5,'{\"status\": \"PENDING\"}','{\"status\": \"APPROVED\"}',0,'2026-05-06 00:59:58',NULL,NULL,NULL),(19,3,'VEHICLE','APPROVE',8,'{\"status\": \"PENDING\"}','{\"status\": \"APPROVED\"}',0,'2026-05-06 01:10:42',NULL,NULL,NULL),(20,5,'VEHICLE','APPROVE',7,'{\"status\": \"PENDING\"}','{\"status\": \"APPROVED\"}',0,'2026-05-06 01:11:03',NULL,NULL,NULL),(21,5,'USER','APPROVE',9,'{\"status\": \"PENDING\"}','{\"status\": \"APPROVED\"}',0,'2026-05-06 01:19:52',NULL,NULL,NULL),(22,5,'RESERVATION','APPROVE',10,'{\"status\": \"PENDING\"}','{\"status\": \"APPROVED\"}',0,'2026-05-06 01:20:42',NULL,NULL,NULL),(23,1,'STORE','CREATE',2,'null','{\"monthlyQuota\": 60, \"ticketPolicyId\": 6, \"ticketPolicyName\": \"[관리자] 2천원 할인권\", \"storeTicketConfigId\": 1}',0,'2026-05-06 01:28:33',NULL,NULL,NULL),(24,5,'RESERVATION','APPROVE',11,'{\"status\": \"PENDING\"}','{\"status\": \"APPROVED\"}',0,'2026-05-06 01:29:25',NULL,NULL,NULL),(25,1,'RESERVATION','APPROVE',12,'{\"status\": \"PENDING\"}','{\"status\": \"APPROVED\"}',0,'2026-05-06 01:38:59',NULL,NULL,NULL),(26,4,'USER','APPROVE',13,'{\"status\": \"PENDING\"}','{\"status\": \"APPROVED\"}',0,'2026-05-06 03:53:29',NULL,NULL,NULL),(27,5,'VEHICLE','BLACKLIST',1,'null','{\"id\": 1, \"status\": \"ACTIVE\", \"endDate\": \"2026-05-06T12:57:00\", \"carNumber\": \"111가1111\", \"createdAt\": \"2026-05-06T12:57:49.455701732\", \"permanent\": false, \"startDate\": \"2026-05-06T12:57:49.455689472\", \"reasonType\": \"ADMIN_MANUAL\", \"releasedAt\": null, \"reasonDetail\": \"상습 불법주차\"}',0,'2026-05-06 03:57:49',NULL,NULL,NULL),(28,4,'USER','APPROVE',15,'{\"status\": \"PENDING\"}','{\"status\": \"APPROVED\"}',0,'2026-05-06 03:57:51',NULL,NULL,NULL),(29,5,'VEHICLE','BLACKLIST',1,'{\"id\": 1, \"status\": \"ACTIVE\", \"endDate\": \"2026-05-06T12:57:00\", \"carNumber\": \"111가1111\", \"createdAt\": \"2026-05-06T12:57:49\", \"permanent\": false, \"startDate\": \"2026-05-06T12:57:49\", \"reasonType\": \"ADMIN_MANUAL\", \"releasedAt\": null, \"reasonDetail\": \"상습 불법주차\"}','{\"id\": 1, \"status\": \"RELEASED\", \"endDate\": \"2026-05-06T12:58:15.178147128\", \"carNumber\": \"111가1111\", \"createdAt\": \"2026-05-06T12:57:49\", \"permanent\": false, \"startDate\": \"2026-05-06T12:57:49\", \"reasonType\": \"ADMIN_MANUAL\", \"releasedAt\": \"2026-05-06T12:58:15.178147128\", \"reasonDetail\": \"상습 불법주차\"}',0,'2026-05-06 03:58:15',NULL,NULL,NULL),(30,5,'VEHICLE','BLACKLIST',2,'null','{\"id\": 2, \"status\": \"ACTIVE\", \"endDate\": \"2026-05-06T12:59:00\", \"carNumber\": \"65노0887\", \"createdAt\": \"2026-05-06T12:59:09.124146283\", \"permanent\": false, \"startDate\": \"2026-05-06T12:59:09.1241398\", \"reasonType\": \"ADMIN_MANUAL\", \"releasedAt\": null, \"reasonDetail\": \"ㅋ\"}',0,'2026-05-06 03:59:09',NULL,NULL,NULL),(31,5,'REPORT','REJECT',4,'{\"status\": \"PENDING\"}','{\"status\": \"REJECTED\", \"rejectReason\": \"이상없음\"}',0,'2026-05-06 04:01:56',NULL,NULL,NULL),(32,5,'REPORT','REJECT',3,'{\"status\": \"PENDING\"}','{\"status\": \"REJECTED\", \"rejectReason\": \"이상없음\"}',0,'2026-05-06 04:02:16',NULL,NULL,NULL),(33,1,'VEHICLE','BLACKLIST',2,'{\"id\": 2, \"status\": \"ACTIVE\", \"endDate\": \"2026-05-06T12:59:00\", \"carNumber\": \"65노0887\", \"createdAt\": \"2026-05-06T12:59:09\", \"permanent\": false, \"startDate\": \"2026-05-06T12:59:09\", \"reasonType\": \"ADMIN_MANUAL\", \"releasedAt\": null, \"reasonDetail\": \"ㅋ\"}','{\"id\": 2, \"status\": \"RELEASED\", \"endDate\": \"2026-05-06T13:05:39.328987601\", \"carNumber\": \"65노0887\", \"createdAt\": \"2026-05-06T12:59:09\", \"permanent\": false, \"startDate\": \"2026-05-06T12:59:09\", \"reasonType\": \"ADMIN_MANUAL\", \"releasedAt\": \"2026-05-06T13:05:39.328987601\", \"reasonDetail\": \"ㅋ\"}',0,'2026-05-06 04:05:39',NULL,NULL,NULL),(34,5,'VEHICLE','BLACKLIST',4,'null','{\"id\": 4, \"status\": \"ACTIVE\", \"endDate\": \"2026-05-07T13:06:00\", \"carNumber\": \"65노0888\", \"createdAt\": \"2026-05-06T13:07:25.630673823\", \"permanent\": false, \"startDate\": \"2026-05-06T13:07:25.630662156\", \"reasonType\": \"USER_BLACKLIST\", \"releasedAt\": null, \"reasonDetail\": \"ㅁ\"}',0,'2026-05-06 04:07:26',NULL,NULL,NULL),(35,5,'VEHICLE','BLACKLIST',4,'{\"id\": 4, \"status\": \"ACTIVE\", \"endDate\": \"2026-05-07T13:06:00\", \"carNumber\": \"65노0888\", \"createdAt\": \"2026-05-06T13:07:26\", \"permanent\": false, \"startDate\": \"2026-05-06T13:07:26\", \"reasonType\": \"USER_BLACKLIST\", \"releasedAt\": null, \"reasonDetail\": \"ㅁ\"}','{\"id\": 4, \"status\": \"RELEASED\", \"endDate\": \"2026-05-06T13:07:39.058206865\", \"carNumber\": \"65노0888\", \"createdAt\": \"2026-05-06T13:07:26\", \"permanent\": false, \"startDate\": \"2026-05-06T13:07:26\", \"reasonType\": \"USER_BLACKLIST\", \"releasedAt\": \"2026-05-06T13:07:39.058206865\", \"reasonDetail\": \"ㅁ\"}',0,'2026-05-06 04:07:39',NULL,NULL,NULL),(36,5,'VEHICLE','BLACKLIST',5,'null','{\"id\": 5, \"status\": \"ACTIVE\", \"endDate\": \"2026-05-07T13:08:00\", \"carNumber\": \"65라4088\", \"createdAt\": \"2026-05-06T13:08:39.181514662\", \"permanent\": false, \"startDate\": \"2026-05-06T13:08:39.181507772\", \"reasonType\": \"USER_BLACKLIST\", \"releasedAt\": null, \"reasonDetail\": \"ㅋ\"}',0,'2026-05-06 04:08:39',NULL,NULL,NULL),(37,5,'VEHICLE','BLACKLIST',5,'{\"id\": 5, \"status\": \"ACTIVE\", \"endDate\": \"2026-05-07T13:08:00\", \"carNumber\": \"65라4088\", \"createdAt\": \"2026-05-06T13:08:39\", \"permanent\": false, \"startDate\": \"2026-05-06T13:08:39\", \"reasonType\": \"USER_BLACKLIST\", \"releasedAt\": null, \"reasonDetail\": \"ㅋ\"}','{\"id\": 5, \"status\": \"RELEASED\", \"endDate\": \"2026-05-06T13:16:18.200991014\", \"carNumber\": \"65라4088\", \"createdAt\": \"2026-05-06T13:08:39\", \"permanent\": false, \"startDate\": \"2026-05-06T13:08:39\", \"reasonType\": \"USER_BLACKLIST\", \"releasedAt\": \"2026-05-06T13:16:18.200991014\", \"reasonDetail\": \"ㅋ\"}',0,'2026-05-06 04:16:18',NULL,NULL,NULL),(38,3,'POLICY','CREATE',5,'{\"id\": 5, \"admin\": {\"name\": \"최주연\", \"status\": \"ACTIVE\", \"adminId\": 3, \"loginId\": \"admin03\", \"password\": \"$2b$10$L1UDD5O2.7oqjhLu7yP63elnvEp.bNBJbLKQDXwIECfjI9wqPanzS\", \"createdAt\": \"2026-05-05T07:45:06\", \"deletedAt\": null, \"lastLoginAt\": \"2026-05-06T14:36:47\"}, \"baseFee\": 1000, \"unitFee\": 800, \"version\": 3, \"isActive\": false, \"createdAt\": \"2026-05-06T05:40:02.493667\", \"dailyMaxFee\": 15000, \"effectiveTo\": \"3000-01-01T00:00:00\", \"parkingType\": \"VISIT\", \"unitMinutes\": 10, \"graceMinutes\": 10, \"effectiveFrom\": \"2026-05-07T00:00:00\"}','{\"id\": 5, \"admin\": {\"name\": \"최주연\", \"status\": \"ACTIVE\", \"adminId\": 3, \"loginId\": \"admin03\", \"password\": \"$2b$10$L1UDD5O2.7oqjhLu7yP63elnvEp.bNBJbLKQDXwIECfjI9wqPanzS\", \"createdAt\": \"2026-05-05T07:45:06\", \"deletedAt\": null, \"lastLoginAt\": \"2026-05-06T14:36:47\"}, \"baseFee\": 1000, \"unitFee\": 800, \"version\": 3, \"isActive\": false, \"createdAt\": \"2026-05-06T05:40:02.493667\", \"dailyMaxFee\": 15000, \"effectiveTo\": \"3000-01-01T00:00:00\", \"parkingType\": \"VISIT\", \"unitMinutes\": 10, \"graceMinutes\": 10, \"effectiveFrom\": \"2026-05-07T00:00:00\"}',0,'2026-05-06 05:40:03',NULL,NULL,NULL),(39,3,'POLICY','UPDATE',4,'{\"id\": 4, \"admin\": {\"name\": \"최주연\", \"status\": \"ACTIVE\", \"adminId\": 3, \"loginId\": \"admin03\", \"password\": \"$2b$10$L1UDD5O2.7oqjhLu7yP63elnvEp.bNBJbLKQDXwIECfjI9wqPanzS\", \"createdAt\": \"2026-05-05T07:45:06\", \"deletedAt\": null, \"lastLoginAt\": \"2026-05-06T14:36:47\"}, \"baseFee\": 1000, \"unitFee\": 700, \"version\": 2, \"isActive\": true, \"createdAt\": \"2026-05-05T12:23:39\", \"dailyMaxFee\": 15000, \"effectiveTo\": \"3000-01-01T00:00:00\", \"parkingType\": \"VISIT\", \"unitMinutes\": 10, \"graceMinutes\": 10, \"effectiveFrom\": \"2026-05-06T00:00:00\"}','{\"id\": 4, \"admin\": {\"name\": \"최주연\", \"status\": \"ACTIVE\", \"adminId\": 3, \"loginId\": \"admin03\", \"password\": \"$2b$10$L1UDD5O2.7oqjhLu7yP63elnvEp.bNBJbLKQDXwIECfjI9wqPanzS\", \"createdAt\": \"2026-05-05T07:45:06\", \"deletedAt\": null, \"lastLoginAt\": \"2026-05-06T14:36:47\"}, \"baseFee\": 1000, \"unitFee\": 700, \"version\": 2, \"isActive\": true, \"createdAt\": \"2026-05-05T12:23:39\", \"dailyMaxFee\": 15000, \"effectiveTo\": \"2026-05-06T23:59:59\", \"parkingType\": \"VISIT\", \"unitMinutes\": 10, \"graceMinutes\": 10, \"effectiveFrom\": \"2026-05-06T00:00:00\"}',0,'2026-05-06 05:40:03',NULL,NULL,NULL),(40,2,'REPORT','APPROVE',1,'{\"status\": \"PENDING\"}','{\"status\": \"APPROVED\", \"validReportCount\": 1}',0,'2026-05-06 05:42:21',NULL,NULL,NULL),(41,2,'PARKING_SPACE','UPDATE',2,'{\"status\": \"AVAILABLE\", \"disabled\": true, \"evCharge\": false, \"spaceCode\": \"B1-002\", \"isDisabled\": true, \"isEvCharge\": false}','{\"status\": \"BLOCKED\", \"disabled\": true, \"evCharge\": false, \"spaceCode\": \"B1-002\", \"isDisabled\": true, \"isEvCharge\": false}',0,'2026-05-06 05:42:30',NULL,NULL,NULL),(42,2,'PARKING_SPACE','UPDATE',5,'{\"status\": \"AVAILABLE\", \"disabled\": false, \"evCharge\": false, \"spaceCode\": \"B1-005\", \"isDisabled\": false, \"isEvCharge\": false}','{\"status\": \"AVAILABLE\", \"disabled\": false, \"evCharge\": true, \"spaceCode\": \"B1-005\", \"isDisabled\": false, \"isEvCharge\": true}',0,'2026-05-06 05:42:38',NULL,NULL,NULL),(43,2,'PARKING_SPACE','UPDATE',13,'{\"status\": \"AVAILABLE\", \"disabled\": false, \"evCharge\": false, \"spaceCode\": \"B1-013\", \"isDisabled\": false, \"isEvCharge\": false}','{\"status\": \"AVAILABLE\", \"disabled\": true, \"evCharge\": false, \"spaceCode\": \"B1-013\", \"isDisabled\": true, \"isEvCharge\": false}',0,'2026-05-06 05:42:48',NULL,NULL,NULL),(44,2,'PARKING_LOG','UPDATE',23,'{\"fee\": 0, \"floor\": \"B2\", \"paidAt\": null, \"rawFee\": 0, \"exitTime\": null, \"exitedAt\": null, \"userType\": \"RESIDENT\", \"blacklist\": false, \"carNumber\": \"123가4568\", \"enteredAt\": \"2026-05-06 14:38:05\", \"entryTime\": \"2026-05-06 05:38:01\", \"spaceCode\": \"B2-029\", \"parkingLogId\": 23, \"calculatedFee\": 0, \"freeExitUntil\": \"3000-01-01 00:00:00\", \"parkingStatus\": \"ENTERED\", \"paymentStatus\": \"NONE\", \"exitPlateImage\": null, \"entryPlateImage\": \"https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260506_143757_19cfd27566c84c53b7255538c9d20f96.jpg\", \"parkingDuration\": \"5분\", \"adminDiscountTotal\": 0, \"paymentRequestedAt\": null, \"storeDiscountTotal\": 0, \"totalDiscountAmount\": 0}','{\"fee\": 0, \"floor\": \"B2\", \"paidAt\": \"2026-05-06 14:43:28\", \"rawFee\": 0, \"exitTime\": null, \"exitedAt\": \"2026-05-06 14:43:28\", \"userType\": \"RESIDENT\", \"blacklist\": false, \"carNumber\": \"123가4568\", \"enteredAt\": \"2026-05-06 14:38:05\", \"entryTime\": \"2026-05-06 05:38:01\", \"spaceCode\": \"B2-029\", \"parkingLogId\": 23, \"calculatedFee\": 0, \"freeExitUntil\": \"3000-01-01 00:00:00\", \"parkingStatus\": \"FORCE_EXITED\", \"paymentStatus\": \"PAID\", \"exitPlateImage\": null, \"entryPlateImage\": \"https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260506_143757_19cfd27566c84c53b7255538c9d20f96.jpg\", \"parkingDuration\": \"5분\", \"adminDiscountTotal\": 0, \"paymentRequestedAt\": null, \"storeDiscountTotal\": 0, \"totalDiscountAmount\": 0}',0,'2026-05-06 05:43:29',NULL,NULL,'{\"reason\": \"관리자 직접 조치 - 테스트중\", \"parkingStatus\": \"FORCE_EXITED\", \"paymentStatus\": \"PAID\"}'),(45,2,'POLICY','CREATE',6,'{\"id\": 6, \"admin\": {\"name\": \"김보경\", \"status\": \"ACTIVE\", \"adminId\": 2, \"loginId\": \"admin02\", \"password\": \"$2b$10$LqvaM0IwX5zBT7hqDim5t.LYky4bJcr7aorkNVrXo08..ZqqyZV1e\", \"createdAt\": \"2026-05-05T07:45:06\", \"deletedAt\": null, \"lastLoginAt\": \"2026-05-06T14:42:11\"}, \"baseFee\": 10000000, \"unitFee\": 1000, \"version\": 4, \"isActive\": false, \"createdAt\": \"2026-05-06T05:44:21.206415\", \"dailyMaxFee\": 200000, \"effectiveTo\": \"3000-01-01T00:00:00\", \"parkingType\": \"VISIT\", \"unitMinutes\": 20, \"graceMinutes\": 30, \"effectiveFrom\": \"2026-05-20T15:00:00\"}','{\"id\": 6, \"admin\": {\"name\": \"김보경\", \"status\": \"ACTIVE\", \"adminId\": 2, \"loginId\": \"admin02\", \"password\": \"$2b$10$LqvaM0IwX5zBT7hqDim5t.LYky4bJcr7aorkNVrXo08..ZqqyZV1e\", \"createdAt\": \"2026-05-05T07:45:06\", \"deletedAt\": null, \"lastLoginAt\": \"2026-05-06T14:42:11\"}, \"baseFee\": 10000000, \"unitFee\": 1000, \"version\": 4, \"isActive\": false, \"createdAt\": \"2026-05-06T05:44:21.206415\", \"dailyMaxFee\": 200000, \"effectiveTo\": \"3000-01-01T00:00:00\", \"parkingType\": \"VISIT\", \"unitMinutes\": 20, \"graceMinutes\": 30, \"effectiveFrom\": \"2026-05-20T15:00:00\"}',0,'2026-05-06 05:44:21',NULL,NULL,NULL),(46,2,'POLICY','UPDATE',5,'{}','{}',0,'2026-05-06 05:44:21',NULL,NULL,NULL),(47,2,'PARKING_LOG','UPDATE',20,'{\"fee\": 0, \"floor\": \"B1\", \"paidAt\": null, \"rawFee\": 1700, \"exitTime\": null, \"exitedAt\": null, \"userType\": \"VISIT\", \"blacklist\": false, \"carNumber\": \"145보6946\", \"enteredAt\": \"2026-05-06 14:33:32\", \"entryTime\": \"2026-05-06 05:33:30\", \"spaceCode\": \"B1-019\", \"parkingLogId\": 20, \"calculatedFee\": 1700, \"freeExitUntil\": \"2026-05-06 14:43:32\", \"parkingStatus\": \"ENTERED\", \"paymentStatus\": \"UNPAID\", \"exitPlateImage\": null, \"entryPlateImage\": \"https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260506_143328_a81c5fac6f92430ea9d8be5c64465308.jpg\", \"parkingDuration\": \"11분\", \"adminDiscountTotal\": 0, \"paymentRequestedAt\": null, \"storeDiscountTotal\": 0, \"totalDiscountAmount\": 0}','{\"fee\": 0, \"floor\": \"B1\", \"paidAt\": null, \"rawFee\": 1700, \"exitTime\": null, \"exitedAt\": null, \"userType\": \"VISIT\", \"blacklist\": false, \"carNumber\": \"145보6946\", \"enteredAt\": \"2026-05-06 14:33:32\", \"entryTime\": \"2026-05-06 05:33:30\", \"spaceCode\": \"B1-019\", \"parkingLogId\": 20, \"calculatedFee\": 0, \"freeExitUntil\": \"2026-05-06 14:43:32\", \"parkingStatus\": \"ENTERED\", \"paymentStatus\": \"NONE\", \"exitPlateImage\": null, \"entryPlateImage\": \"https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260506_143328_a81c5fac6f92430ea9d8be5c64465308.jpg\", \"parkingDuration\": \"11분\", \"adminDiscountTotal\": 2000, \"paymentRequestedAt\": null, \"storeDiscountTotal\": 0, \"totalDiscountAmount\": 2000}',0,'2026-05-06 05:44:35',NULL,NULL,'{\"reason\": \"관리자 직권 할인: [관리자] 2천원 할인권 - 테스트\", \"totalDiscount\": 2000, \"appliedPolicyName\": \"[관리자] 2천원 할인권\", \"finalCalculatedFee\": 0, \"adminDiscountAmount\": 2000}'),(48,2,'PARKING_SPACE','UPDATE',1,'{\"status\": \"AVAILABLE\", \"disabled\": false, \"evCharge\": false, \"spaceCode\": \"B1-001\", \"isDisabled\": false, \"isEvCharge\": false}','{\"status\": \"AVAILABLE\", \"disabled\": true, \"evCharge\": false, \"spaceCode\": \"B1-001\", \"isDisabled\": true, \"isEvCharge\": false}',0,'2026-05-06 05:45:18',NULL,NULL,NULL),(49,2,'PARKING_SPACE','UPDATE',2,'{\"status\": \"BLOCKED\", \"disabled\": true, \"evCharge\": false, \"spaceCode\": \"B1-002\", \"isDisabled\": true, \"isEvCharge\": false}','{\"status\": \"AVAILABLE\", \"disabled\": true, \"evCharge\": false, \"spaceCode\": \"B1-002\", \"isDisabled\": true, \"isEvCharge\": false}',0,'2026-05-06 05:45:27',NULL,NULL,NULL),(50,2,'STORE','ACTIVE',3,'{\"name\": \"1층 약국\", \"status\": \"INACTIVE\", \"terminalPassword\": \"1234\"}','{\"name\": \"1층 약국\", \"status\": \"ACTIVE\", \"terminalPassword\": \"969696\"}',0,'2026-05-06 05:49:07',NULL,NULL,NULL),(51,2,'STORE','CREATE',3,'null','{\"monthlyQuota\": 100, \"ticketPolicyId\": 6, \"ticketPolicyName\": \"[관리자] 2천원 할인권\", \"storeTicketConfigId\": 2}',0,'2026-05-06 05:49:41',NULL,NULL,NULL),(52,2,'STORE','INACTIVE',3,'{\"name\": \"1층 약국\", \"status\": \"ACTIVE\", \"terminalPassword\": \"969696\"}','{\"name\": \"1층 약국\", \"status\": \"INACTIVE\", \"terminalPassword\": \"969696\"}',0,'2026-05-06 05:50:19',NULL,NULL,NULL),(53,2,'RESERVATION_POLICY','CREATE',2,'{\"id\": 2, \"eventName\": \"새로운정책\", \"startDate\": \"2026-05-06T14:51:00\", \"adminLoginId\": \"admin02\", \"policyStatus\": \"PERMANENT\", \"permittedMinutes\": 60, \"noShowPenaltyEnabled\": true, \"maxActiveReservations\": 2, \"dailyLimitPerHousehold\": 10, \"monthlyLimitPerHousehold\": 100}','{\"id\": 2, \"eventName\": \"새로운정책\", \"startDate\": \"2026-05-06T14:51:00\", \"adminLoginId\": \"admin02\", \"policyStatus\": \"PERMANENT\", \"permittedMinutes\": 60, \"noShowPenaltyEnabled\": true, \"maxActiveReservations\": 2, \"dailyLimitPerHousehold\": 10, \"monthlyLimitPerHousehold\": 100}',0,'2026-05-06 05:51:34',NULL,NULL,NULL),(54,2,'USER','APPROVE',19,'{\"status\": \"PENDING\"}','{\"status\": \"APPROVED\"}',0,'2026-05-06 05:59:02',NULL,NULL,NULL),(55,2,'RESERVATION','APPROVE',20,'{\"status\": \"PENDING\"}','{\"status\": \"APPROVED\"}',0,'2026-05-06 05:59:31',NULL,NULL,NULL),(56,2,'VEHICLE','BLACKLIST',6,'null','{\"id\": 6, \"status\": \"ACTIVE\", \"endDate\": \"3000-01-01T00:00:00\", \"carNumber\": \"222가2222\", \"createdAt\": \"2026-05-06T15:05:48.089770141\", \"permanent\": true, \"startDate\": \"2026-05-06T15:05:48.089764876\", \"reasonType\": \"USER_BLACKLIST\", \"releasedAt\": null, \"reasonDetail\": \"ㅁㅁ\"}',0,'2026-05-06 06:05:48',NULL,NULL,NULL),(57,2,'VEHICLE','BLACKLIST',6,'{\"id\": 6, \"status\": \"ACTIVE\", \"endDate\": \"3000-01-01T00:00:00\", \"carNumber\": \"222가2222\", \"createdAt\": \"2026-05-06T15:05:48\", \"permanent\": true, \"startDate\": \"2026-05-06T15:05:48\", \"reasonType\": \"USER_BLACKLIST\", \"releasedAt\": null, \"reasonDetail\": \"ㅁㅁ\"}','{\"id\": 6, \"status\": \"RELEASED\", \"endDate\": \"2026-05-06T15:06:05.991481456\", \"carNumber\": \"222가2222\", \"createdAt\": \"2026-05-06T15:05:48\", \"permanent\": false, \"startDate\": \"2026-05-06T15:05:48\", \"reasonType\": \"USER_BLACKLIST\", \"releasedAt\": \"2026-05-06T15:06:05.991481456\", \"reasonDetail\": \"ㅁㅁ\"}',0,'2026-05-06 06:06:06',NULL,NULL,NULL),(58,4,'RESERVATION','REJECT',21,'{\"status\": \"PENDING\"}','{\"status\": \"REJECTED\", \"rejectReason\": \"ㅇㅇ\"}',0,'2026-05-06 06:24:41',NULL,NULL,NULL),(59,4,'USER','REJECT',22,'{\"status\": \"PENDING\"}','{\"status\": \"REJECTED\", \"rejectReason\": \"ㅇㅇ\"}',0,'2026-05-06 06:24:44',NULL,NULL,NULL),(60,1,'PARKING_SPACE','UPDATE',13,'{\"status\": \"AVAILABLE\", \"disabled\": true, \"evCharge\": false, \"spaceCode\": \"B1-013\", \"isDisabled\": true, \"isEvCharge\": false}','{\"status\": \"AVAILABLE\", \"disabled\": false, \"evCharge\": false, \"spaceCode\": \"B1-013\", \"isDisabled\": false, \"isEvCharge\": false}',0,'2026-05-08 02:46:25',NULL,NULL,NULL),(61,2,'PARKING_LOG','UPDATE',27,'{\"fee\": 0, \"floor\": \"B1\", \"paidAt\": null, \"rawFee\": 30000, \"exitTime\": null, \"exitedAt\": null, \"userType\": \"USER\", \"blacklist\": false, \"carNumber\": \"02로0202\", \"enteredAt\": \"2026-05-06 15:20:19\", \"entryTime\": \"2026-05-06 06:20:17\", \"spaceCode\": \"B1-006\", \"parkingLogId\": 27, \"calculatedFee\": 30000, \"freeExitUntil\": \"2026-05-06 15:30:19\", \"parkingStatus\": \"ENTERED\", \"paymentStatus\": \"UNPAID\", \"exitPlateImage\": null, \"entryPlateImage\": \"https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260506_152014_35b9cfc90f0f4840a936869c4aed4a7d.jpg\", \"parkingDuration\": \"46시간 40분\", \"adminDiscountTotal\": 0, \"paymentRequestedAt\": null, \"storeDiscountTotal\": 0, \"totalDiscountAmount\": 0}','{\"fee\": 0, \"floor\": \"B1\", \"paidAt\": \"2026-05-08 14:00:44\", \"rawFee\": 30000, \"exitTime\": null, \"exitedAt\": \"2026-05-08 14:00:44\", \"userType\": \"USER\", \"blacklist\": false, \"carNumber\": \"02로0202\", \"enteredAt\": \"2026-05-06 15:20:19\", \"entryTime\": \"2026-05-06 06:20:17\", \"spaceCode\": \"B1-006\", \"parkingLogId\": 27, \"calculatedFee\": 0, \"freeExitUntil\": \"2026-05-06 15:30:19\", \"parkingStatus\": \"FORCE_EXITED\", \"paymentStatus\": \"PAID\", \"exitPlateImage\": null, \"entryPlateImage\": \"https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260506_152014_35b9cfc90f0f4840a936869c4aed4a7d.jpg\", \"parkingDuration\": \"46시간 40분\", \"adminDiscountTotal\": 0, \"paymentRequestedAt\": null, \"storeDiscountTotal\": 0, \"totalDiscountAmount\": 0}',0,'2026-05-08 05:00:45',NULL,NULL,'{\"reason\": \"관리자 직접 조치\", \"parkingStatus\": \"FORCE_EXITED\", \"paymentStatus\": \"PAID\"}'),(62,1,'REPORT','APPROVE',5,'{\"status\": \"PENDING\"}','{\"status\": \"APPROVED\", \"validReportCount\": 1}',0,'2026-05-08 05:10:53',NULL,NULL,NULL),(63,1,'PARKING_SPACE','UPDATE',32,'{\"status\": \"AVAILABLE\", \"disabled\": false, \"evCharge\": false, \"spaceCode\": \"B2-001\", \"isDisabled\": false, \"isEvCharge\": false}','{\"status\": \"AVAILABLE\", \"disabled\": true, \"evCharge\": false, \"spaceCode\": \"B2-001\", \"isDisabled\": true, \"isEvCharge\": false}',0,'2026-05-08 05:11:23',NULL,NULL,NULL),(64,1,'PARKING_SPACE','UPDATE',33,'{\"status\": \"AVAILABLE\", \"disabled\": false, \"evCharge\": false, \"spaceCode\": \"B2-002\", \"isDisabled\": false, \"isEvCharge\": false}','{\"status\": \"AVAILABLE\", \"disabled\": false, \"evCharge\": true, \"spaceCode\": \"B2-002\", \"isDisabled\": false, \"isEvCharge\": true}',0,'2026-05-08 05:11:25',NULL,NULL,NULL),(65,2,'RESERVATION','APPROVE',23,'{\"status\": \"PENDING\"}','{\"status\": \"APPROVED\"}',0,'2026-05-10 08:06:41',NULL,NULL,NULL),(66,2,'PARKING_SPACE','UPDATE',3,'{\"status\": \"AVAILABLE\", \"disabled\": false, \"evCharge\": false, \"spaceCode\": \"B1-003\", \"isDisabled\": false, \"isEvCharge\": false}','{\"status\": \"BLOCKED\", \"disabled\": false, \"evCharge\": false, \"spaceCode\": \"B1-003\", \"isDisabled\": false, \"isEvCharge\": false}',0,'2026-05-10 08:07:16',NULL,NULL,NULL),(67,2,'PARKING_SPACE','UPDATE',2,'{\"status\": \"AVAILABLE\", \"disabled\": true, \"evCharge\": false, \"spaceCode\": \"B1-002\", \"isDisabled\": true, \"isEvCharge\": false}','{\"status\": \"AVAILABLE\", \"disabled\": false, \"evCharge\": true, \"spaceCode\": \"B1-002\", \"isDisabled\": false, \"isEvCharge\": true}',0,'2026-05-10 08:07:21',NULL,NULL,NULL),(68,1,'PARKING_SPACE','UPDATE',3,'{\"status\": \"BLOCKED\", \"disabled\": false, \"evCharge\": false, \"spaceCode\": \"B1-003\", \"isDisabled\": false, \"isEvCharge\": false}','{\"status\": \"AVAILABLE\", \"disabled\": false, \"evCharge\": false, \"spaceCode\": \"B1-003\", \"isDisabled\": false, \"isEvCharge\": false}',0,'2026-05-11 09:02:34',NULL,NULL,NULL),(69,1,'PARKING_SPACE','UPDATE',3,'{\"status\": \"AVAILABLE\", \"disabled\": false, \"evCharge\": false, \"spaceCode\": \"B1-003\", \"isDisabled\": false, \"isEvCharge\": false}','{\"status\": \"AVAILABLE\", \"disabled\": false, \"evCharge\": true, \"spaceCode\": \"B1-003\", \"isDisabled\": false, \"isEvCharge\": true}',0,'2026-05-11 09:56:47',NULL,NULL,NULL),(70,1,'PARKING_SPACE','UPDATE',4,'{\"status\": \"AVAILABLE\", \"disabled\": false, \"evCharge\": false, \"spaceCode\": \"B1-004\", \"isDisabled\": false, \"isEvCharge\": false}','{\"status\": \"AVAILABLE\", \"disabled\": true, \"evCharge\": false, \"spaceCode\": \"B1-004\", \"isDisabled\": true, \"isEvCharge\": false}',0,'2026-05-15 04:17:35',NULL,NULL,NULL),(71,1,'PARKING_SPACE','UPDATE',5,'{\"status\": \"AVAILABLE\", \"disabled\": false, \"evCharge\": true, \"spaceCode\": \"B1-005\", \"isDisabled\": false, \"isEvCharge\": true}','{\"status\": \"AVAILABLE\", \"disabled\": false, \"evCharge\": false, \"spaceCode\": \"B1-005\", \"isDisabled\": false, \"isEvCharge\": false}',0,'2026-05-15 13:37:26',NULL,NULL,NULL),(72,1,'REPORT','APPROVE',2,'{\"status\": \"PENDING\"}','{\"status\": \"APPROVED\", \"validReportCount\": 1}',0,'2026-05-15 13:40:11',NULL,NULL,NULL),(73,1,'PARKING_SPACE','UPDATE',3,'{\"status\": \"AVAILABLE\", \"disabled\": false, \"evCharge\": true, \"spaceCode\": \"B1-003\", \"isDisabled\": false, \"isEvCharge\": true}','{\"status\": \"AVAILABLE\", \"disabled\": false, \"evCharge\": false, \"spaceCode\": \"B1-003\", \"isDisabled\": false, \"isEvCharge\": false}',0,'2026-05-15 17:06:24',NULL,NULL,NULL),(74,1,'PARKING_SPACE','UPDATE',2,'{\"status\": \"AVAILABLE\", \"disabled\": false, \"evCharge\": true, \"spaceCode\": \"B1-002\", \"isDisabled\": false, \"isEvCharge\": true}','{\"status\": \"AVAILABLE\", \"disabled\": false, \"evCharge\": false, \"spaceCode\": \"B1-002\", \"isDisabled\": false, \"isEvCharge\": false}',0,'2026-05-17 23:27:04',NULL,NULL,NULL),(75,6,'PARKING_SPACE','UPDATE',2,'{\"status\": \"AVAILABLE\", \"disabled\": false, \"evCharge\": false, \"spaceCode\": \"B1-002\", \"isDisabled\": false, \"isEvCharge\": false}','{\"status\": \"AVAILABLE\", \"disabled\": false, \"evCharge\": true, \"spaceCode\": \"B1-002\", \"isDisabled\": false, \"isEvCharge\": true}',0,'2026-05-21 11:50:27',NULL,NULL,NULL),(76,6,'PARKING_SPACE','UPDATE',6,'{\"status\": \"AVAILABLE\", \"disabled\": false, \"evCharge\": false, \"spaceCode\": \"B1-006\", \"isDisabled\": false, \"isEvCharge\": false}','{\"status\": \"BLOCKED\", \"disabled\": false, \"evCharge\": false, \"spaceCode\": \"B1-006\", \"isDisabled\": false, \"isEvCharge\": false}',0,'2026-05-21 11:50:40',NULL,NULL,NULL);
/*!40000 ALTER TABLE `admin_action_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `admin_chat_message`
--

DROP TABLE IF EXISTS `admin_chat_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin_chat_message` (
  `message_id` bigint NOT NULL AUTO_INCREMENT,
  `room_id` bigint NOT NULL,
  `sender_id` bigint NOT NULL COMMENT '메시지를 보낸 관리자 ID',
  `content` text NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`message_id`),
  KEY `fk_msg_sender` (`sender_id`),
  KEY `idx_acm_room_created` (`room_id`,`created_at`),
  CONSTRAINT `fk_msg_room` FOREIGN KEY (`room_id`) REFERENCES `admin_chat_room` (`room_id`),
  CONSTRAINT `fk_msg_sender` FOREIGN KEY (`sender_id`) REFERENCES `admin` (`admin_id`)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin_chat_message`
--

LOCK TABLES `admin_chat_message` WRITE;
/*!40000 ALTER TABLE `admin_chat_message` DISABLE KEYS */;
INSERT INTO `admin_chat_message` VALUES (1,1,1,'채팅','2026-05-13 07:35:07'),(2,2,1,'테스트','2026-05-13 07:35:15'),(3,4,5,'ㅗㅑ','2026-05-13 08:02:31'),(4,4,5,'hi','2026-05-13 08:02:32'),(5,4,1,'test','2026-05-13 08:03:16'),(6,4,5,'rrrrr','2026-05-13 08:04:12'),(7,6,1,'그룹 채팅방입니다','2026-05-13 08:04:36'),(8,7,8,'test','2026-05-13 08:04:45'),(9,7,5,'aaa','2026-05-13 08:04:54'),(10,7,1,'test','2026-05-13 08:05:44'),(11,9,1,'하이','2026-05-14 07:51:21'),(12,10,6,'ㅎㅇ','2026-05-14 07:52:57'),(13,11,6,'채팅','2026-05-14 07:57:03'),(14,10,6,'ㅎㅇ','2026-05-14 08:01:52'),(15,10,6,'ㅎㅇ','2026-05-14 08:01:52'),(16,6,3,'뭐요','2026-05-14 08:05:46'),(17,1,1,'dd','2026-05-15 03:24:03'),(18,1,1,'dt','2026-05-15 03:24:06'),(19,1,1,'123','2026-05-15 03:49:27'),(20,1,1,'1234','2026-05-15 04:14:08'),(21,1,1,'1234','2026-05-15 13:37:16'),(22,12,6,'뭐함','2026-05-18 13:34:11'),(23,12,1,'테스트','2026-05-18 13:34:29'),(24,12,1,'테스트2','2026-05-18 13:49:02'),(25,12,1,'테스트','2026-05-18 14:07:00'),(26,12,1,'테스트','2026-05-18 14:15:15'),(27,12,1,'테스트','2026-05-18 14:15:19'),(28,13,6,'안녕','2026-05-18 18:01:13'),(29,12,6,'테스트','2026-05-20 16:16:14'),(30,12,1,'확인','2026-05-20 16:16:24'),(31,12,6,'확인','2026-05-20 16:16:33');
/*!40000 ALTER TABLE `admin_chat_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `admin_chat_room`
--

DROP TABLE IF EXISTS `admin_chat_room`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin_chat_room` (
  `room_id` bigint NOT NULL AUTO_INCREMENT,
  `room_type` enum('DIRECT','GROUP') NOT NULL COMMENT '1:1 or 그룹',
  `room_name` varchar(100) DEFAULT NULL COMMENT '그룹 채팅방 이름 (DIRECT면 NULL)',
  `created_by` bigint NOT NULL COMMENT '채팅방 생성 관리자',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`room_id`),
  KEY `fk_room_creator` (`created_by`),
  CONSTRAINT `fk_room_creator` FOREIGN KEY (`created_by`) REFERENCES `admin` (`admin_id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin_chat_room`
--

LOCK TABLES `admin_chat_room` WRITE;
/*!40000 ALTER TABLE `admin_chat_room` DISABLE KEYS */;
INSERT INTO `admin_chat_room` VALUES (1,'DIRECT',NULL,1,'2026-05-13 07:20:18'),(2,'DIRECT',NULL,1,'2026-05-13 07:21:19'),(3,'DIRECT',NULL,1,'2026-05-13 07:36:43'),(4,'DIRECT',NULL,5,'2026-05-13 08:02:28'),(5,'DIRECT',NULL,8,'2026-05-13 08:04:07'),(6,'GROUP','테스트방',1,'2026-05-13 08:04:26'),(7,'GROUP','test',8,'2026-05-13 08:04:37'),(8,'DIRECT',NULL,2,'2026-05-14 06:29:29'),(9,'GROUP','그룹채팅',1,'2026-05-14 07:50:02'),(10,'DIRECT',NULL,6,'2026-05-14 07:52:54'),(11,'DIRECT',NULL,6,'2026-05-14 07:56:53'),(12,'DIRECT',NULL,6,'2026-05-18 13:34:04'),(13,'DIRECT',NULL,6,'2026-05-18 18:01:11'),(14,'GROUP','테스트용 그룹채팅',8,'2026-05-18 18:16:12');
/*!40000 ALTER TABLE `admin_chat_room` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `admin_chat_room_member`
--

DROP TABLE IF EXISTS `admin_chat_room_member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin_chat_room_member` (
  `member_id` bigint NOT NULL AUTO_INCREMENT,
  `room_id` bigint NOT NULL,
  `admin_id` bigint NOT NULL,
  `last_read_message_id` bigint DEFAULT NULL COMMENT '마지막으로 읽은 메시지 ID (안읽은 메시지 수 계산용)',
  `joined_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`member_id`),
  UNIQUE KEY `uq_room_admin` (`room_id`,`admin_id`),
  UNIQUE KEY `UK6dtshn0cb4lp0xpsw06oxokjx` (`room_id`,`admin_id`),
  KEY `idx_acrm_admin_room` (`admin_id`,`room_id`),
  CONSTRAINT `fk_member_admin` FOREIGN KEY (`admin_id`) REFERENCES `admin` (`admin_id`),
  CONSTRAINT `fk_member_room` FOREIGN KEY (`room_id`) REFERENCES `admin_chat_room` (`room_id`)
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin_chat_room_member`
--

LOCK TABLES `admin_chat_room_member` WRITE;
/*!40000 ALTER TABLE `admin_chat_room_member` DISABLE KEYS */;
INSERT INTO `admin_chat_room_member` VALUES (1,1,1,21,'2026-05-13 07:20:18'),(2,2,1,2,'2026-05-13 07:21:19'),(3,2,2,NULL,'2026-05-13 07:21:19'),(4,3,1,NULL,'2026-05-13 07:36:43'),(5,3,3,NULL,'2026-05-13 07:36:43'),(6,4,5,6,'2026-05-13 08:02:28'),(7,4,1,6,'2026-05-13 08:02:28'),(8,5,8,NULL,'2026-05-13 08:04:07'),(9,5,5,NULL,'2026-05-13 08:04:07'),(10,6,1,16,'2026-05-13 08:04:26'),(11,6,5,NULL,'2026-05-13 08:04:26'),(12,6,2,7,'2026-05-13 08:04:26'),(13,6,3,7,'2026-05-13 08:04:26'),(14,6,4,NULL,'2026-05-13 08:04:26'),(15,7,8,10,'2026-05-13 08:04:37'),(16,7,1,10,'2026-05-13 08:04:37'),(18,8,2,NULL,'2026-05-14 06:29:30'),(19,8,3,NULL,'2026-05-14 06:29:30'),(20,9,1,11,'2026-05-14 07:50:02'),(22,9,7,NULL,'2026-05-14 07:50:02'),(23,10,6,15,'2026-05-14 07:52:54'),(24,10,7,NULL,'2026-05-14 07:52:54'),(25,11,6,13,'2026-05-14 07:56:53'),(26,12,6,31,'2026-05-18 13:34:04'),(27,12,1,31,'2026-05-18 13:34:04'),(28,13,6,28,'2026-05-18 18:01:11'),(29,13,8,NULL,'2026-05-18 18:01:11'),(30,14,8,NULL,'2026-05-18 18:16:12'),(31,14,1,NULL,'2026-05-18 18:16:12'),(32,14,2,NULL,'2026-05-18 18:16:12'),(33,14,3,NULL,'2026-05-18 18:16:12'),(34,14,4,NULL,'2026-05-18 18:16:12'),(35,14,5,NULL,'2026-05-18 18:16:12');
/*!40000 ALTER TABLE `admin_chat_room_member` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `approval`
--

DROP TABLE IF EXISTS `approval`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `approval` (
  `approval_id` bigint NOT NULL AUTO_INCREMENT,
  `approval_type` enum('RESIDENT','VEHICLE','RESERVATION','REPORT') NOT NULL COMMENT '승인 유형',
  `target_id` bigint NOT NULL COMMENT '해당 유형 테이블의 PK',
  `request_user_id` bigint DEFAULT NULL COMMENT '요청한 유저 ID',
  `status` enum('PENDING','APPROVED','REJECTED','CANCELLED') DEFAULT 'PENDING' COMMENT '현재 처리 상태',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '승인 요청 일시',
  `processed_at` datetime DEFAULT NULL COMMENT '관리자 처리 완료 일시',
  `processed_by_admin_id` bigint DEFAULT NULL COMMENT '처리를 담당한 관리자 ID',
  `reject_reason` text COMMENT '거절 시 사유 (사용자 안내용)',
  PRIMARY KEY (`approval_id`),
  KEY `fk_app_admin` (`processed_by_admin_id`),
  KEY `idx_app_status` (`status`),
  KEY `idx_app_user_type_status` (`request_user_id`,`approval_type`,`status`),
  KEY `idx_app_target_type_status` (`target_id`,`approval_type`,`status`),
  CONSTRAINT `fk_app_admin` FOREIGN KEY (`processed_by_admin_id`) REFERENCES `admin` (`admin_id`),
  CONSTRAINT `fk_app_user` FOREIGN KEY (`request_user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `approval`
--

LOCK TABLES `approval` WRITE;
/*!40000 ALTER TABLE `approval` DISABLE KEYS */;
INSERT INTO `approval` VALUES (1,'RESIDENT',2,3,'APPROVED','2026-05-06 09:37:43','2026-05-06 09:42:45',1,NULL),(2,'RESIDENT',20,4,'APPROVED','2026-05-06 09:38:00','2026-05-06 09:40:52',1,NULL),(3,'VEHICLE',1,3,'APPROVED','2026-05-06 09:39:14','2026-05-06 09:42:43',1,NULL),(4,'RESERVATION',1,3,'APPROVED','2026-05-06 09:43:22','2026-05-06 09:44:04',1,NULL),(5,'RESIDENT',1,6,'APPROVED','2026-05-06 09:57:47','2026-05-06 09:59:58',1,NULL),(6,'VEHICLE',2,6,'APPROVED','2026-05-06 09:58:35','2026-05-06 09:59:58',1,NULL),(7,'VEHICLE',3,5,'APPROVED','2026-05-06 10:10:32','2026-05-06 10:11:03',5,NULL),(8,'VEHICLE',4,7,'APPROVED','2026-05-06 10:10:34','2026-05-06 10:10:42',3,NULL),(9,'RESIDENT',3,5,'APPROVED','2026-05-06 10:19:46','2026-05-06 10:19:52',5,NULL),(10,'RESERVATION',2,5,'APPROVED','2026-05-06 10:20:30','2026-05-06 10:20:42',5,NULL),(11,'RESERVATION',3,5,'APPROVED','2026-05-06 10:29:11','2026-05-06 10:29:25',5,NULL),(12,'RESERVATION',4,6,'APPROVED','2026-05-06 10:31:36','2026-05-06 10:38:59',1,NULL),(13,'RESIDENT',1,6,'APPROVED','2026-05-06 12:53:22','2026-05-06 12:53:29',4,NULL),(14,'RESERVATION',5,6,'CANCELLED','2026-05-06 12:55:47',NULL,NULL,NULL),(15,'RESIDENT',1,6,'APPROVED','2026-05-06 12:57:46','2026-05-06 12:57:51',4,NULL),(16,'VEHICLE',2,9,'APPROVED','2026-05-06 14:57:31','2026-05-06 14:57:31',NULL,NULL),(17,'VEHICLE',2,9,'CANCELLED','2026-05-06 14:57:57','2026-05-06 14:58:00',NULL,'사용자가 신청을 취소하였습니다.'),(18,'VEHICLE',2,9,'APPROVED','2026-05-06 14:58:13','2026-05-06 14:58:13',NULL,NULL),(19,'RESIDENT',1,9,'APPROVED','2026-05-06 14:58:58','2026-05-06 14:59:02',2,NULL),(20,'RESERVATION',6,9,'APPROVED','2026-05-06 14:59:15','2026-05-06 14:59:31',2,NULL),(21,'RESERVATION',7,9,'REJECTED','2026-05-06 15:00:44','2026-05-06 15:24:41',4,'ㅇㅇ'),(22,'RESIDENT',1,9,'REJECTED','2026-05-06 15:01:15','2026-05-06 15:24:44',4,'ㅇㅇ'),(23,'RESERVATION',8,4,'APPROVED','2026-05-10 17:06:26','2026-05-10 17:06:41',2,NULL);
/*!40000 ALTER TABLE `approval` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `camera`
--

DROP TABLE IF EXISTS `camera`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `camera` (
  `camera_id` bigint NOT NULL AUTO_INCREMENT,
  `camera_code` varchar(100) NOT NULL,
  `description` text,
  `location` varchar(200) NOT NULL,
  `floor` enum('B1','B2') NOT NULL,
  `camera_type` enum('ENTRY','EXIT','AREA') NOT NULL,
  `rtsp_url` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`camera_id`),
  UNIQUE KEY `camera_code` (`camera_code`),
  KEY `idx_camera_type` (`camera_type`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `camera`
--

LOCK TABLES `camera` WRITE;
/*!40000 ALTER TABLE `camera` DISABLE KEYS */;
INSERT INTO `camera` VALUES (1,'ENTRY-A','입구 게이트 A 카메라','지하주차장 입구 게이트 A','B1','ENTRY',NULL),(2,'ENTRY-B','입구 게이트 B 카메라','지하주차장 입구 게이트 B','B1','ENTRY',NULL),(3,'EXIT-A','출구 게이트 A 카메라','지하주차장 출구 게이트 A','B1','EXIT',NULL),(4,'EXIT-B','출구 게이트 B 카메라','지하주차장 출구 게이트 B','B1','EXIT',NULL),(5,'AREA-B1-1','B1층 내부 카메라 1','B1층 구역 1 (A열)','B1','AREA',NULL),(6,'AREA-B1-2','B1층 내부 카메라 2','B1층 구역 2 (B열)','B1','AREA',NULL),(7,'AREA-B2-1','B2층 내부 카메라 1','B2층 구역 1 (A열)','B2','AREA',NULL),(8,'AREA-B2-2','B2층 내부 카메라 2','B2층 구역 2 (B열)','B2','AREA',NULL);
/*!40000 ALTER TABLE `camera` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `camera_recording`
--

DROP TABLE IF EXISTS `camera_recording`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `camera_recording` (
  `camera_recording_id` bigint NOT NULL AUTO_INCREMENT,
  `camera_id` bigint NOT NULL,
  `file_path` varchar(255) NOT NULL,
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `file_size` bigint NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`camera_recording_id`),
  KEY `fk_camera_recording_camera` (`camera_id`),
  CONSTRAINT `fk_camera_recording_camera` FOREIGN KEY (`camera_id`) REFERENCES `camera` (`camera_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `camera_recording`
--

LOCK TABLES `camera_recording` WRITE;
/*!40000 ALTER TABLE `camera_recording` DISABLE KEYS */;
/*!40000 ALTER TABLE `camera_recording` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `household`
--

DROP TABLE IF EXISTS `household`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `household` (
  `household_id` bigint NOT NULL AUTO_INCREMENT,
  `unit_no` int NOT NULL,
  `is_active` enum('ACTIVE','INACTIVE') NOT NULL DEFAULT 'INACTIVE',
  `total_visit_count` int NOT NULL DEFAULT '0',
  `today_visit_count` int NOT NULL DEFAULT '0',
  `monthly_visit_count` int NOT NULL DEFAULT '0',
  `active_reservation_count` int NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`household_id`),
  UNIQUE KEY `unit_no` (`unit_no`),
  KEY `idx_hh_is_active` (`is_active`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `household`
--

LOCK TABLES `household` WRITE;
/*!40000 ALTER TABLE `household` DISABLE KEYS */;
INSERT INTO `household` VALUES (1,101,'INACTIVE',0,0,0,0,'2026-05-05 07:45:06'),(2,102,'ACTIVE',1,0,0,0,'2026-05-05 07:45:06'),(3,103,'ACTIVE',2,0,0,0,'2026-05-05 07:45:06'),(4,104,'INACTIVE',0,0,0,0,'2026-05-05 07:45:06'),(5,105,'INACTIVE',0,0,0,0,'2026-05-05 07:45:06'),(6,106,'INACTIVE',0,0,0,0,'2026-05-05 07:45:06'),(7,107,'INACTIVE',0,0,0,0,'2026-05-05 07:45:06'),(8,108,'INACTIVE',0,0,0,0,'2026-05-05 07:45:06'),(9,109,'INACTIVE',0,0,0,0,'2026-05-05 07:45:06'),(10,110,'INACTIVE',0,0,0,0,'2026-05-05 07:45:06'),(11,111,'INACTIVE',0,0,0,0,'2026-05-05 07:45:06'),(12,112,'INACTIVE',0,0,0,0,'2026-05-05 07:45:06'),(13,113,'INACTIVE',0,0,0,0,'2026-05-05 07:45:06'),(14,114,'INACTIVE',0,0,0,0,'2026-05-05 07:45:06'),(15,115,'INACTIVE',0,0,0,0,'2026-05-05 07:45:06'),(16,116,'INACTIVE',0,0,0,0,'2026-05-05 07:45:06'),(17,117,'INACTIVE',0,0,0,0,'2026-05-05 07:45:06'),(18,118,'INACTIVE',0,0,0,0,'2026-05-05 07:45:06'),(19,119,'INACTIVE',0,0,0,0,'2026-05-05 07:45:06'),(20,120,'ACTIVE',1,0,0,0,'2026-05-05 07:45:06');
/*!40000 ALTER TABLE `household` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notification`
--

DROP TABLE IF EXISTS `notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification` (
  `notification_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '알림을 받는 유저 ID',
  `type` enum('PAYMENT','RESERVATION','EVENT','WARNING','SYSTEM','REFUNDED') NOT NULL COMMENT '알림 유형',
  `title` varchar(255) NOT NULL COMMENT '알림 제목',
  `content` varchar(255) NOT NULL COMMENT '알림 본문 내용',
  `read_at` datetime DEFAULT NULL COMMENT '사용자가 알림을 확인한 시각 (NULL이면 미확인)',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '알림 생성일',
  `status` enum('ACTIVE','DELETED') NOT NULL DEFAULT 'ACTIVE' COMMENT '알림 노출 상태',
  `deleted_at` datetime DEFAULT NULL COMMENT '알림 삭제 일시',
  PRIMARY KEY (`notification_id`),
  KEY `idx_noti_user_status` (`user_id`,`status`),
  KEY `idx_noti_user_read_status` (`user_id`,`read_at`,`status`),
  CONSTRAINT `fk_not_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notification`
--

LOCK TABLES `notification` WRITE;
/*!40000 ALTER TABLE `notification` DISABLE KEYS */;
INSERT INTO `notification` VALUES (1,4,'SYSTEM','입주민 등록 완료','120호 입주민 등록이 완료되었습니다.',NULL,'2026-05-06 00:40:52','ACTIVE',NULL),(2,3,'SYSTEM','차량 등록 완료','123가4568차량 등록이 승인되었습니다.','2026-05-20 16:23:15','2026-05-06 00:42:44','DELETED','2026-05-20 17:21:16'),(3,3,'SYSTEM','입주민 등록 완료','102호 입주민 등록이 완료되었습니다.','2026-05-20 16:38:51','2026-05-06 00:42:45','DELETED','2026-05-20 17:21:14'),(4,3,'RESERVATION','방문 예약 승인','154러7070 방문 예약이 승인되었습니다.','2026-05-20 16:23:13','2026-05-06 00:44:04','DELETED','2026-05-20 17:21:10'),(5,6,'SYSTEM','차량 등록 완료','12거3456차량 등록이 승인되었습니다.','2026-05-06 12:00:00','2026-05-06 00:59:58','ACTIVE','2026-05-06 12:22:35'),(6,6,'SYSTEM','입주민 등록 완료','101호 입주민 등록이 완료되었습니다.','2026-05-06 12:00:01','2026-05-06 00:59:58','ACTIVE','2026-05-06 12:22:35'),(7,7,'SYSTEM','차량 등록 완료','02로0202차량 등록이 승인되었습니다.',NULL,'2026-05-06 01:10:43','ACTIVE',NULL),(8,5,'SYSTEM','차량 등록 완료','69도3445차량 등록이 승인되었습니다.','2026-05-06 10:30:52','2026-05-06 01:11:03','ACTIVE',NULL),(9,5,'SYSTEM','입주민 등록 완료','103호 입주민 등록이 완료되었습니다.','2026-05-06 15:04:09','2026-05-06 01:19:53','ACTIVE',NULL),(10,5,'RESERVATION','방문 예약 승인','000가0000 방문 예약이 승인되었습니다.','2026-05-21 11:31:27','2026-05-06 01:20:42','ACTIVE',NULL),(11,5,'RESERVATION','방문 예약 승인','65라4088 방문 예약이 승인되었습니다.',NULL,'2026-05-06 01:29:25','ACTIVE',NULL),(12,5,'WARNING','차량 신고 접수 알림','[69도3445] 차량이 \'이중 주차\'로 신고되었습니다.',NULL,'2026-05-06 01:38:27','ACTIVE',NULL),(13,6,'RESERVATION','방문 예약 승인','11나1234 방문 예약이 승인되었습니다.','2026-05-06 11:59:55','2026-05-06 01:39:00','ACTIVE','2026-05-06 12:22:35'),(14,6,'SYSTEM','입주민 등록 완료','101호 입주민 등록이 완료되었습니다.',NULL,'2026-05-06 03:53:30','ACTIVE','2026-05-06 12:57:07'),(15,6,'SYSTEM','입주민 등록 완료','101호 입주민 등록이 완료되었습니다.',NULL,'2026-05-06 03:57:51','ACTIVE','2026-05-06 14:14:24'),(16,9,'SYSTEM','입주민 등록 완료','101호 입주민 등록이 완료되었습니다.',NULL,'2026-05-06 05:59:03','ACTIVE',NULL),(17,9,'RESERVATION','방문 예약 승인','11가1234 방문 예약이 승인되었습니다.',NULL,'2026-05-06 05:59:32','ACTIVE',NULL),(18,9,'RESERVATION','방문 예약 반려','11가1234 방문 예약이 반려되었습니다.',NULL,'2026-05-06 06:24:42','ACTIVE',NULL),(19,4,'RESERVATION','방문 예약 승인','12가4568 방문 예약이 승인되었습니다.',NULL,'2026-05-10 08:06:42','ACTIVE',NULL),(20,7,'PAYMENT','결제 완료 알람','정산이 완료되었습니다. 16:06까지 출차해 주세요.',NULL,'2026-05-15 16:01:38','ACTIVE',NULL),(21,7,'PAYMENT','결제 완료 알람','정산이 완료되었습니다. 15:29까지 출차해 주세요.',NULL,'2026-05-20 15:24:52','ACTIVE',NULL),(22,7,'PAYMENT','결제 완료 알람','정산이 완료되었습니다. 16:27까지 출차해 주세요.',NULL,'2026-05-20 16:22:14','ACTIVE',NULL);
/*!40000 ALTER TABLE `notification` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `parking_fee_policy`
--

DROP TABLE IF EXISTS `parking_fee_policy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `parking_fee_policy` (
  `parking_fee_policy_id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint NOT NULL COMMENT '등록한 관리자 ID',
  `parking_type` enum('VISIT','RESERVATION') NOT NULL COMMENT '정책 구분',
  `grace_minutes` int NOT NULL DEFAULT '0' COMMENT '회차 인정 시간 (분)',
  `base_fee` int NOT NULL DEFAULT '0',
  `unit_minutes` int NOT NULL DEFAULT '0' COMMENT '추가 단위 시간 (분)',
  `unit_fee` int NOT NULL DEFAULT '0' COMMENT '추가 단위 요금',
  `daily_max_fee` int NOT NULL DEFAULT '0' COMMENT '일 최대 요금',
  `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '현재 활성화 여부',
  `effective_from` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '적용 시작 시점',
  `effective_to` datetime NOT NULL DEFAULT '3000-01-01 00:00:00' COMMENT '적용 종료 시점',
  `version` bigint DEFAULT '1' COMMENT '정책 버전(수정 시 증가)',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록한 시간',
  PRIMARY KEY (`parking_fee_policy_id`),
  KEY `fk_parking_policy_admin_id` (`admin_id`),
  KEY `idx_pfp_type_active_from` (`parking_type`,`is_active`,`effective_from`),
  CONSTRAINT `fk_parking_policy_admin_id` FOREIGN KEY (`admin_id`) REFERENCES `admin` (`admin_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `parking_fee_policy`
--

LOCK TABLES `parking_fee_policy` WRITE;
/*!40000 ALTER TABLE `parking_fee_policy` DISABLE KEYS */;
INSERT INTO `parking_fee_policy` VALUES (1,1,'VISIT',10,1000,10,500,15000,0,'2026-05-05 12:02:05','2026-05-05 23:59:59',1,'2026-05-05 12:02:05'),(2,1,'RESERVATION',15,500,10,300,10000,0,'2026-05-05 12:02:05','2026-05-05 23:59:59',1,'2026-05-05 12:02:05'),(3,3,'RESERVATION',15,500,10,500,10000,1,'2026-05-06 00:00:00','3000-01-01 00:00:00',2,'2026-05-05 12:19:48'),(4,3,'VISIT',10,1000,10,700,15000,0,'2026-05-06 00:00:00','2026-05-06 23:59:59',2,'2026-05-05 12:23:39'),(5,3,'VISIT',10,1000,10,800,15000,0,'2026-05-07 00:00:00','2026-05-20 14:59:59',3,'2026-05-06 05:40:02'),(6,2,'VISIT',30,10000000,20,1000,200000,1,'2026-05-20 15:00:00','3000-01-01 00:00:00',4,'2026-05-06 05:44:21');
/*!40000 ALTER TABLE `parking_fee_policy` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `parking_log`
--

DROP TABLE IF EXISTS `parking_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `parking_log` (
  `parking_log_id` bigint NOT NULL AUTO_INCREMENT,
  `vehicle_id` bigint DEFAULT NULL,
  `parking_space_id` bigint DEFAULT NULL,
  `car_number_snapshot` varchar(25) NOT NULL COMMENT '인식된 번호판 번호',
  `entry_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '입차 감지 시점',
  `exit_time` datetime DEFAULT NULL COMMENT '출차 감지 시점',
  `entry_camera_id` bigint DEFAULT NULL COMMENT '입차 인식 카메라',
  `exit_camera_id` bigint DEFAULT NULL COMMENT '출차 인식 카메라',
  `parking_type_snapshot` enum('RESIDENT','VISIT','USER','RESERVATION','SUBSCRIPTION') NOT NULL COMMENT '입차 시점 권한',
  `is_blacklist` tinyint(1) NOT NULL DEFAULT '0',
  `fee` int NOT NULL DEFAULT '0' COMMENT '최종 결제 금액',
  `latest_order_id` varchar(255) DEFAULT NULL,
  `payment_status` enum('NONE','UNPAID','PAID','REFUNDED') NOT NULL DEFAULT 'NONE',
  `parking_status` enum('DETECTED','ENTRY_CANCELLED','ENTERED','EXIT_REQUESTED','EXITED','FORCE_EXITED','BLACKLIST_REJECTED') DEFAULT 'DETECTED',
  `parking_fee_policy_id` bigint NOT NULL COMMENT '적용된 요금 정책 ID',
  `calculated_fee` bigint NOT NULL DEFAULT '0' COMMENT '계산된 발생 요금',
  `entered_at` datetime DEFAULT NULL COMMENT '실제 입차 완료(게이트 통과)',
  `exited_at` datetime DEFAULT NULL COMMENT '실제 출차 완료(세션 종료)',
  `paid_at` datetime DEFAULT NULL COMMENT '결제 완료 시점',
  `free_exit_until` datetime DEFAULT NULL COMMENT '무료 출차 가능 데드라인',
  `grace_minutes_snapshot` bigint NOT NULL COMMENT '입차 시점 회차 시간(분) 실제 계산되는 값',
  `entry_plate_image` varchar(512) NOT NULL,
  `exit_plate_image` varchar(512) DEFAULT NULL,
  `raw_fee` int NOT NULL DEFAULT '0' COMMENT '할인 받기 전 순수요금',
  `total_discount_minutes` int NOT NULL DEFAULT '0',
  `total_discount_amount` int NOT NULL DEFAULT '0',
  `payment_requested_at` datetime DEFAULT NULL COMMENT '요금 조회 및 결제 요청 시점 검증',
  PRIMARY KEY (`parking_log_id`),
  KEY `fk_log_space` (`parking_space_id`),
  KEY `fk_log_policy` (`parking_fee_policy_id`),
  KEY `fk_log_entry_camera` (`entry_camera_id`),
  KEY `fk_log_exit_camera` (`exit_camera_id`),
  KEY `idx_pl_car_status` (`car_number_snapshot`,`parking_status`),
  KEY `idx_pl_parking_status` (`parking_status`),
  KEY `idx_pl_payment_status` (`payment_status`),
  KEY `idx_pl_entry_time` (`entry_time`),
  KEY `idx_pl_exited_at` (`exited_at`),
  KEY `idx_pl_vehicle_status` (`vehicle_id`,`parking_status`),
  CONSTRAINT `fk_log_entry_camera` FOREIGN KEY (`entry_camera_id`) REFERENCES `camera` (`camera_id`),
  CONSTRAINT `fk_log_exit_camera` FOREIGN KEY (`exit_camera_id`) REFERENCES `camera` (`camera_id`),
  CONSTRAINT `fk_log_policy` FOREIGN KEY (`parking_fee_policy_id`) REFERENCES `parking_fee_policy` (`parking_fee_policy_id`),
  CONSTRAINT `fk_log_space` FOREIGN KEY (`parking_space_id`) REFERENCES `parking_space` (`parking_space_id`),
  CONSTRAINT `fk_log_vehicle` FOREIGN KEY (`vehicle_id`) REFERENCES `vehicle` (`vehicle_id`)
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `parking_log`
--

LOCK TABLES `parking_log` WRITE;
/*!40000 ALTER TABLE `parking_log` DISABLE KEYS */;
INSERT INTO `parking_log` VALUES (1,NULL,16,'356가7890','2026-05-06 00:21:20','2026-05-06 09:25:11',1,3,'VISIT',0,0,NULL,'NONE','EXITED',4,0,'2026-05-06 09:21:24','2026-05-06 09:25:13',NULL,'2026-05-06 09:31:24',10,'https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260506_092114_384cf1a2417549bca571a6df8e0c864b.jpg','https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260506_092507_4a01cfe86f474315ae9e84ccc146f0b1.jpg',0,0,0,NULL),(2,NULL,26,'65노0887','2026-05-06 00:32:39','2026-05-06 12:59:39',1,3,'VISIT',0,15000,'6ec0f23c-0e14-4081-be1b-91dd4ba92e3c','PAID','EXITED',4,15000,'2026-05-06 09:32:42','2026-05-06 12:59:56','2026-05-06 12:59:54','2026-05-06 13:04:54',10,'https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260506_093238_874fe1e933224c2fa9d6f239442d20a1.jpg','https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260506_125937_a90b76f2ae9a49bb95d881d62027c4c4.jpg',15000,0,0,'2026-05-06 12:59:43'),(3,NULL,3,'02로0202','2026-05-06 00:34:01','2026-05-06 14:35:17',1,3,'VISIT',0,5000,'44a75077-1091-4950-80a0-f452cdb7087b','PAID','EXITED',4,5000,'2026-05-06 09:34:03','2026-05-06 14:35:57','2026-05-06 14:35:35','2026-05-06 14:40:35',10,'https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260506_093359_46341c85940149138bfd0c36990503d7.jpg','https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260506_143500_69da07010ace459094c7a50367c51113.jpg',15000,0,10000,'2026-05-06 14:35:19'),(4,NULL,27,'57서9757','2026-05-06 00:34:26',NULL,1,NULL,'VISIT',0,0,'c830e6f2-02da-4107-b7c0-24d223f2eaf1','UNPAID','ENTERED',4,15000,'2026-05-06 09:34:28',NULL,NULL,'2026-05-06 09:44:28',10,'https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260506_093423_2c3ae496f06347f694aae15cab7366eb.jpg',NULL,15000,0,0,'2026-05-06 13:15:09'),(5,NULL,30,'69도3445','2026-05-06 00:35:48','2026-05-06 10:18:56',1,3,'VISIT',0,3800,'1d0aeaa4-4ec7-46e1-86aa-d78601e194e2','PAID','EXITED',4,3800,'2026-05-06 09:35:50','2026-05-06 10:19:15','2026-05-06 10:19:14','2026-05-06 10:24:14',10,'https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260506_093547_a9e69b2346174de5824addb6559fa00a.jpg','https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260506_101854_584b6dbe931b4c4e881839fe4ce46ed9.jpg',3800,0,0,'2026-05-06 10:18:58'),(6,3,32,'69도3445','2026-05-06 01:21:12','2026-05-06 10:40:08',1,3,'RESIDENT',0,0,NULL,'NONE','EXITED',4,0,'2026-05-06 10:21:15','2026-05-06 10:40:09',NULL,'3000-01-01 00:00:00',10,'https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260506_102111_2298ee7be9db49b7ab6396e8cf4eac70.jpg','https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260506_104006_3e24738047ca46389bd5d8cd4b4a6937.jpg',0,0,0,NULL),(7,NULL,12,'65라4088','2026-05-06 01:29:51','2026-05-06 13:09:29',1,3,'VISIT',0,11500,'331d48c1-4684-41e1-a36c-d8148f598b4c','PAID','EXITED',4,11500,'2026-05-06 10:29:54','2026-05-06 13:09:44','2026-05-06 13:09:42','2026-05-06 13:14:42',10,'https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260506_102949_485e6219dc7f4a419bb1fa923f3f4780.jpg','https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260506_130919_234a291ea76a476fa0d8a3de335497be.jpg',11500,0,0,'2026-05-06 13:09:31'),(8,NULL,NULL,'356가7890','2026-05-06 01:31:39',NULL,1,NULL,'VISIT',0,0,NULL,'NONE','ENTRY_CANCELLED',4,0,NULL,NULL,NULL,NULL,10,'https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260506_103133_d274cd8b1a93469c8ccd1c7e21390018.jpg',NULL,0,0,0,NULL),(9,3,32,'69도3445','2026-05-06 02:11:17','2026-05-06 13:17:08',1,3,'RESIDENT',0,0,NULL,'NONE','EXITED',4,0,'2026-05-06 11:11:19','2026-05-06 13:17:10',NULL,'3000-01-01 00:00:00',10,'https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260506_111116_df6ee2b1931940e1b3a2eaaab11d1814.jpg','https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260506_131706_49784f084aef4a5dbae7a084741ff6f7.jpg',0,0,0,NULL),(10,NULL,30,'391서8288','2026-05-06 03:09:43',NULL,2,NULL,'VISIT',0,9400,'a6be92b2-b7eb-49af-b3f3-ac3e218504b0','UNPAID','ENTERED',4,9400,'2026-05-06 12:09:47',NULL,'2026-05-06 14:13:25','2026-05-06 14:18:25',10,'https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260506_120942_becc7c28540947ac83799233c84576f6.jpg',NULL,9400,0,0,NULL),(11,NULL,NULL,'65노0887','2026-05-06 04:00:05',NULL,1,NULL,'VISIT',0,0,NULL,'NONE','ENTRY_CANCELLED',4,0,NULL,NULL,NULL,NULL,10,'https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260506_130003_b0899a064a5d44f1874511f41eb3b890.jpg',NULL,0,0,0,NULL),(12,NULL,NULL,'65노0887','2026-05-06 04:03:25',NULL,1,NULL,'VISIT',0,0,NULL,'NONE','ENTRY_CANCELLED',4,0,NULL,NULL,NULL,NULL,10,'https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260506_130323_41b7cf423e8c486e9d5dc4468d902639.jpg',NULL,0,0,0,NULL),(15,3,54,'69도3445','2026-05-06 04:17:36','2026-05-06 13:17:58',1,3,'RESIDENT',0,0,NULL,'NONE','EXITED',4,0,'2026-05-06 13:17:39','2026-05-06 13:18:00',NULL,'3000-01-01 00:00:00',10,'https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260506_131734_80370834ee4c44de831e4f295f18fc8a.jpg','https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260506_131757_af6507ce17b54882bcb48a785eaae847.jpg',0,0,0,NULL),(16,NULL,15,'137로2805','2026-05-06 05:14:41',NULL,1,NULL,'VISIT',0,2400,'8773dec1-8bdd-4fb2-bbe9-9325c87a50be','UNPAID','ENTERED',4,2400,'2026-05-06 14:14:43',NULL,'2026-05-06 14:37:57','2026-05-06 14:42:57',10,'https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260506_141434_e50e7d9911ca4da6b415c334fdb26845.jpg',NULL,2400,0,0,NULL),(17,NULL,16,'65라4088','2026-05-06 05:17:10',NULL,1,NULL,'VISIT',0,0,'dd76d0f9-b47a-4497-af54-87eacde6643b','UNPAID','ENTERED',4,2400,'2026-05-06 14:17:12',NULL,NULL,'2026-05-06 14:27:12',10,'https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260506_141708_3da01cd78cbb49ba97f9f66f36f0c78a.jpg',NULL,2400,0,0,'2026-05-06 14:38:48'),(18,1,NULL,'123가4568','2026-05-06 05:32:23',NULL,1,NULL,'RESIDENT',0,0,NULL,'NONE','ENTRY_CANCELLED',4,0,NULL,NULL,NULL,NULL,10,'https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260506_143205_757529d264aa43558840fb94ad7cb49e.jpg',NULL,0,0,0,NULL),(19,3,NULL,'69도3445','2026-05-06 05:32:49',NULL,1,NULL,'RESIDENT',0,0,NULL,'NONE','ENTRY_CANCELLED',4,0,NULL,NULL,NULL,NULL,10,'https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260506_143233_e9bd284c0e1c47c38f48d4e2bc18f902.jpg',NULL,0,0,0,NULL),(20,NULL,19,'145보6946','2026-05-06 05:33:30',NULL,1,NULL,'VISIT',0,0,NULL,'NONE','ENTERED',4,0,'2026-05-06 14:33:32',NULL,NULL,'2026-05-06 14:43:32',10,'https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260506_143328_a81c5fac6f92430ea9d8be5c64465308.jpg',NULL,1700,0,1700,NULL),(21,1,56,'123가4568','2026-05-06 05:33:40','2026-05-06 14:34:05',2,3,'RESIDENT',0,0,NULL,'NONE','EXITED',4,0,'2026-05-06 14:33:43','2026-05-06 14:34:07',NULL,'3000-01-01 00:00:00',10,'https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260506_143338_0377934da5a1492ba9b4e6844fa386ed.jpg','https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260506_143400_22c57faa5d7440c7be4513484bb509e0.jpg',0,0,0,NULL),(22,1,47,'123가4568','2026-05-06 05:36:17','2026-05-06 14:37:41',1,3,'RESIDENT',0,0,NULL,'NONE','EXITED',4,0,'2026-05-06 14:36:18','2026-05-06 14:37:44',NULL,'3000-01-01 00:00:00',10,'https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260506_143615_2a2ab428e0fc495684266f1e3d104f4a.jpg','https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260506_143737_4153be61a2c4418b8db9562cb5550a27.jpg',0,0,0,NULL),(23,1,60,'123가4568','2026-05-06 05:38:01',NULL,2,NULL,'RESIDENT',0,0,NULL,'PAID','FORCE_EXITED',4,0,'2026-05-06 14:38:05','2026-05-06 14:43:29','2026-05-06 14:43:29','3000-01-01 00:00:00',10,'https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260506_143757_19cfd27566c84c53b7255538c9d20f96.jpg',NULL,0,0,0,NULL),(24,1,47,'123가4568','2026-05-06 05:49:16','2026-05-06 14:50:29',1,4,'RESIDENT',0,0,NULL,'NONE','EXITED',4,0,'2026-05-06 14:49:17','2026-05-06 14:50:31',NULL,'3000-01-01 00:00:00',10,'https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260506_144913_015f0cd76d5346078618446fcee23d14.jpg','https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260506_145025_5e010db6c31546b1a0f5ccc631f44d49.jpg',0,0,0,NULL),(25,1,NULL,'123가4568','2026-05-06 05:50:37',NULL,1,NULL,'RESIDENT',0,0,NULL,'NONE','ENTRY_CANCELLED',4,0,NULL,NULL,NULL,NULL,10,'https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260506_145035_d09d4178c365460abc1609441e714b3c.jpg',NULL,0,0,0,NULL),(26,3,42,'69도3445','2026-05-06 06:06:50','2026-05-06 15:07:13',1,3,'RESIDENT',0,0,NULL,'NONE','EXITED',4,0,'2026-05-06 15:06:52','2026-05-06 15:07:14',NULL,'3000-01-01 00:00:00',10,'https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260506_150648_195b7d96384f4fd8b249cd0991e2ceaa.jpg','https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260506_150712_5ab683d9f8ee43ffaeafa798bb490a61.jpg',0,0,0,NULL),(27,4,6,'02로0202','2026-05-06 06:20:17',NULL,2,NULL,'USER',0,0,NULL,'PAID','FORCE_EXITED',4,0,'2026-05-06 15:20:19','2026-05-08 14:00:45','2026-05-08 14:00:45','2026-05-06 15:30:19',10,'https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260506_152014_35b9cfc90f0f4840a936869c4aed4a7d.jpg',NULL,30000,0,30000,NULL),(28,4,26,'02로0202','2026-05-08 06:41:38','2026-05-14 17:04:07',1,4,'SUBSCRIPTION',0,0,NULL,'NONE','EXITED',5,0,'2026-05-08 15:41:40','2026-05-14 17:04:08',NULL,'2026-06-06 00:00:00',10,'https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260508_154133_be234b4fee5440779885ee1f7a8c5bba.jpg','https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260514_170405_f2cd8dde0129474ab1b9ea00316ba52e.jpg',0,0,0,NULL),(29,NULL,NULL,'157고4895','2026-05-10 08:15:44',NULL,1,NULL,'VISIT',0,0,NULL,'NONE','ENTRY_CANCELLED',5,0,NULL,NULL,NULL,NULL,10,'https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260510_171525_235e2e8e458344989c08fc45ba92c654.jpg',NULL,0,0,0,NULL),(30,NULL,14,'157고4895','2026-05-11 05:15:11',NULL,1,NULL,'VISIT',0,150000,'baf4243f-6d5a-4d70-a89d-00925cb51ecc','PAID','ENTERED',5,150000,'2026-05-11 14:15:16',NULL,'2026-05-20 17:21:28','2026-05-20 17:26:28',10,'https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260511_141507_070a588899fb4b348e100b47d55b43fa.jpg',NULL,150000,0,0,'2026-05-20 17:21:17'),(31,3,34,'69도3445','2026-05-13 08:24:40',NULL,1,NULL,'RESIDENT',0,0,NULL,'NONE','ENTERED',5,0,'2026-05-13 17:24:42',NULL,NULL,'3000-01-01 00:00:00',10,'https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260513_172433_6bc77998c9434721b1d744756e2ebbdb.jpg',NULL,0,0,0,NULL),(32,4,NULL,'02로0202','2026-05-14 08:04:27',NULL,1,NULL,'USER',0,0,NULL,'NONE','ENTRY_CANCELLED',5,0,NULL,NULL,NULL,NULL,10,'https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260514_170425_8579ea32a2484a048e1eb393f4e36a87.jpg',NULL,0,0,0,NULL),(33,4,26,'02로0202','2026-05-14 08:04:46',NULL,2,NULL,'USER',0,88000,'08883ae1-475f-4f39-9355-0e30f2bdb5e5','UNPAID','ENTERED',5,88000,'2026-05-14 17:04:48',NULL,'2026-05-20 16:22:14','2026-05-20 16:27:14',10,'https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260514_170445_51992dce143940fb88700320d23fb231.jpg',NULL,90000,600,2000,NULL),(34,1,46,'123가4568','2026-05-19 11:46:06','2026-05-19 11:46:49',1,3,'RESIDENT',0,0,NULL,'NONE','EXITED',5,0,'2026-05-19 11:46:08','2026-05-19 11:46:53',NULL,'3000-01-01 00:00:00',10,'https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260519_114602_29ecfcc319f9440f9339986bdb2f670d.jpg','https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260519_114639_cc150c4d29da4d44a2aa8aacf2371557.jpg',0,0,0,NULL);
/*!40000 ALTER TABLE `parking_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `parking_space`
--

DROP TABLE IF EXISTS `parking_space`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `parking_space` (
  `parking_space_id` bigint NOT NULL AUTO_INCREMENT,
  `space_code` varchar(50) NOT NULL,
  `floor` enum('B1','B2') NOT NULL,
  `is_reservation` tinyint(1) NOT NULL DEFAULT '0',
  `status` enum('AVAILABLE','OCCUPIED','BLOCKED') NOT NULL DEFAULT 'AVAILABLE',
  `last_status_changed_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_disabled` tinyint(1) NOT NULL DEFAULT '0' COMMENT '장애인 전용자리',
  `is_ev_charge` tinyint(1) NOT NULL DEFAULT '0' COMMENT '전기차 전용자리',
  PRIMARY KEY (`parking_space_id`),
  UNIQUE KEY `space_code` (`space_code`),
  KEY `idx_ps_floor_status` (`floor`,`status`),
  KEY `idx_ps_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=63 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `parking_space`
--

LOCK TABLES `parking_space` WRITE;
/*!40000 ALTER TABLE `parking_space` DISABLE KEYS */;
INSERT INTO `parking_space` VALUES (1,'B1-001','B1',0,'AVAILABLE','2026-05-06 05:45:18',1,0),(2,'B1-002','B1',0,'AVAILABLE','2026-05-21 11:50:27',0,1),(3,'B1-003','B1',0,'AVAILABLE','2026-05-15 17:06:24',0,0),(4,'B1-004','B1',0,'AVAILABLE','2026-05-15 04:17:35',1,0),(5,'B1-005','B1',0,'AVAILABLE','2026-05-15 13:37:26',0,0),(6,'B1-006','B1',0,'BLOCKED','2026-05-21 11:50:40',0,0),(7,'B1-007','B1',0,'AVAILABLE','2026-05-05 07:45:06',0,0),(8,'B1-008','B1',0,'AVAILABLE','2026-05-05 07:45:06',0,0),(9,'B1-009','B1',0,'AVAILABLE','2026-05-05 07:45:06',0,0),(10,'B1-010','B1',0,'AVAILABLE','2026-05-05 07:45:06',0,0),(11,'B1-011','B1',0,'AVAILABLE','2026-05-05 07:45:06',0,0),(12,'B1-012','B1',0,'AVAILABLE','2026-05-06 04:09:44',0,0),(13,'B1-013','B1',0,'AVAILABLE','2026-05-08 02:46:25',0,0),(14,'B1-014','B1',0,'OCCUPIED','2026-05-11 05:15:16',0,0),(15,'B1-015','B1',0,'OCCUPIED','2026-05-06 05:14:43',0,0),(16,'B1-016','B1',0,'OCCUPIED','2026-05-06 05:17:12',0,0),(17,'B1-017','B1',0,'AVAILABLE','2026-05-05 07:45:06',0,0),(18,'B1-018','B1',0,'AVAILABLE','2026-05-05 07:45:06',0,0),(19,'B1-019','B1',0,'OCCUPIED','2026-05-06 05:33:32',0,0),(20,'B1-020','B1',0,'AVAILABLE','2026-05-05 07:45:06',0,0),(21,'B1-021','B1',0,'AVAILABLE','2026-05-05 07:45:06',0,0),(22,'B1-022','B1',0,'AVAILABLE','2026-05-05 07:45:06',0,0),(23,'B1-023','B1',0,'AVAILABLE','2026-05-05 07:45:06',0,0),(24,'B1-024','B1',0,'AVAILABLE','2026-05-05 07:45:06',0,0),(25,'B1-025','B1',0,'AVAILABLE','2026-05-05 07:45:06',0,0),(26,'B1-026','B1',0,'OCCUPIED','2026-05-14 08:04:48',0,0),(27,'B1-027','B1',0,'OCCUPIED','2026-05-06 00:34:28',0,0),(28,'B1-028','B1',0,'AVAILABLE','2026-05-05 07:45:06',0,0),(29,'B1-029','B1',0,'AVAILABLE','2026-05-05 07:45:06',0,0),(30,'B1-030','B1',0,'OCCUPIED','2026-05-06 03:09:48',0,0),(32,'B2-001','B2',0,'AVAILABLE','2026-05-08 05:11:23',1,0),(33,'B2-002','B2',0,'AVAILABLE','2026-05-08 05:11:25',0,1),(34,'B2-003','B2',0,'OCCUPIED','2026-05-13 08:24:42',0,0),(35,'B2-004','B2',0,'AVAILABLE','2026-05-05 07:45:06',0,0),(36,'B2-005','B2',0,'AVAILABLE','2026-05-05 07:45:06',0,0),(37,'B2-006','B2',0,'AVAILABLE','2026-05-05 07:45:06',0,0),(38,'B2-007','B2',0,'AVAILABLE','2026-05-05 07:45:06',0,0),(39,'B2-008','B2',0,'AVAILABLE','2026-05-05 07:45:06',0,0),(40,'B2-009','B2',0,'AVAILABLE','2026-05-05 07:45:06',0,0),(41,'B2-010','B2',0,'AVAILABLE','2026-05-05 07:45:06',0,0),(42,'B2-011','B2',0,'AVAILABLE','2026-05-06 06:07:14',0,0),(43,'B2-012','B2',0,'AVAILABLE','2026-05-05 07:45:06',0,0),(44,'B2-013','B2',0,'AVAILABLE','2026-05-05 07:45:06',0,0),(45,'B2-014','B2',0,'AVAILABLE','2026-05-05 07:45:06',0,0),(46,'B2-015','B2',0,'AVAILABLE','2026-05-19 11:46:53',0,0),(47,'B2-016','B2',0,'AVAILABLE','2026-05-06 05:50:31',0,0),(48,'B2-017','B2',0,'AVAILABLE','2026-05-05 07:45:06',0,0),(49,'B2-018','B2',0,'AVAILABLE','2026-05-05 07:45:06',0,0),(50,'B2-019','B2',0,'AVAILABLE','2026-05-05 07:45:06',0,0),(51,'B2-020','B2',0,'AVAILABLE','2026-05-05 07:45:06',0,0),(52,'B2-021','B2',0,'AVAILABLE','2026-05-05 07:45:06',0,0),(53,'B2-022','B2',0,'AVAILABLE','2026-05-05 07:45:06',0,0),(54,'B2-023','B2',0,'AVAILABLE','2026-05-06 04:18:00',0,0),(55,'B2-024','B2',0,'AVAILABLE','2026-05-05 07:45:06',0,0),(56,'B2-025','B2',0,'AVAILABLE','2026-05-06 05:34:07',0,0),(57,'B2-026','B2',0,'AVAILABLE','2026-05-05 07:45:06',0,0),(58,'B2-027','B2',0,'AVAILABLE','2026-05-05 07:45:06',0,0),(59,'B2-028','B2',0,'AVAILABLE','2026-05-05 07:45:06',0,0),(60,'B2-029','B2',0,'AVAILABLE','2026-05-06 05:43:29',0,0),(61,'B2-030','B2',0,'AVAILABLE','2026-05-05 07:45:06',0,0);
/*!40000 ALTER TABLE `parking_space` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `parking_ticket`
--

DROP TABLE IF EXISTS `parking_ticket`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `parking_ticket` (
  `parking_ticket_id` bigint NOT NULL AUTO_INCREMENT,
  `store_id` bigint NOT NULL,
  `parking_log_id` bigint NOT NULL,
  `ticket_policy_id` bigint NOT NULL,
  `status` enum('ADMIN','STORE') NOT NULL DEFAULT 'STORE',
  `applied_amount` int NOT NULL,
  PRIMARY KEY (`parking_ticket_id`),
  KEY `fk_pt_store` (`store_id`),
  KEY `fk_pt_log` (`parking_log_id`),
  KEY `fk_pt_policy` (`ticket_policy_id`),
  CONSTRAINT `fk_pt_log` FOREIGN KEY (`parking_log_id`) REFERENCES `parking_log` (`parking_log_id`),
  CONSTRAINT `fk_pt_policy` FOREIGN KEY (`ticket_policy_id`) REFERENCES `ticket_policy` (`ticket_policy_id`),
  CONSTRAINT `fk_pt_store` FOREIGN KEY (`store_id`) REFERENCES `store` (`store_id`)
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `parking_ticket`
--

LOCK TABLES `parking_ticket` WRITE;
/*!40000 ALTER TABLE `parking_ticket` DISABLE KEYS */;
INSERT INTO `parking_ticket` VALUES (1,2,3,6,'STORE',2000),(2,2,3,6,'STORE',2000),(3,2,3,6,'STORE',2000),(4,2,3,6,'STORE',2000),(5,2,3,6,'STORE',2000),(6,2,23,6,'STORE',0),(7,2,23,6,'STORE',0),(8,2,23,6,'STORE',0),(9,2,23,6,'STORE',0),(10,2,23,6,'STORE',0),(11,2,23,6,'STORE',0),(12,2,23,6,'STORE',0),(13,2,23,6,'STORE',0),(14,2,23,6,'STORE',0),(15,2,23,6,'STORE',0),(16,2,23,6,'STORE',0),(17,2,23,6,'STORE',0),(18,2,23,6,'STORE',0),(19,2,23,6,'STORE',0),(20,2,23,6,'STORE',0),(21,2,23,6,'STORE',0),(22,2,23,6,'STORE',0),(23,2,23,6,'STORE',0),(24,2,23,6,'STORE',0),(25,2,23,6,'STORE',0),(26,1,20,6,'ADMIN',2000),(27,2,33,6,'STORE',2000),(28,2,33,5,'STORE',120),(29,2,33,5,'STORE',120),(30,2,33,5,'STORE',120),(31,2,33,5,'STORE',120),(32,2,33,5,'STORE',120);
/*!40000 ALTER TABLE `parking_ticket` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payment`
--

DROP TABLE IF EXISTS `payment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payment` (
  `payment_id` bigint NOT NULL AUTO_INCREMENT,
  `parking_log_id` bigint DEFAULT NULL COMMENT '출차 결제 시 참조',
  `store_id` bigint DEFAULT NULL COMMENT '할인권 구매 시 참조',
  `vehicle_id` bigint DEFAULT NULL COMMENT '정기권 결제 시 참조',
  `amount` bigint NOT NULL DEFAULT '0' COMMENT '실제로 사용자가 지불(승인)한 금액',
  `price_snapshot` bigint NOT NULL COMMENT '할인이 적용된 후 사용자가 최종적으로 내야 할 청구 금액',
  `payment_method` enum('PAY','POINT','FREE_POLICY') NOT NULL,
  `payment_status` enum('READY','SUCCESS','FAILED','CANCELLED','REFUNDED') NOT NULL DEFAULT 'READY',
  `payment_type` enum('PARKING','SUBSCRIPTION','TICKET') NOT NULL,
  `ticket_quantity` int DEFAULT NULL COMMENT '할인권 구매 시 수량',
  `external_payment_id` varchar(255) DEFAULT NULL COMMENT '결제 후 받는 고유 id',
  `paid_at` datetime DEFAULT NULL COMMENT '실제 결제가 성공한 시각',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `refunded_amount` bigint NOT NULL COMMENT '환불처리된 누적 금액',
  PRIMARY KEY (`payment_id`),
  KEY `fk_pay_store` (`store_id`),
  KEY `fk_pay_vehicle` (`vehicle_id`),
  KEY `idx_pay_log_status` (`parking_log_id`,`payment_status`),
  KEY `idx_pay_external_id` (`external_payment_id`),
  KEY `idx_pay_status_paid` (`payment_status`,`paid_at`),
  KEY `idx_pay_type_status` (`payment_type`,`payment_status`),
  CONSTRAINT `fk_pay_log` FOREIGN KEY (`parking_log_id`) REFERENCES `parking_log` (`parking_log_id`),
  CONSTRAINT `fk_pay_store` FOREIGN KEY (`store_id`) REFERENCES `store` (`store_id`),
  CONSTRAINT `fk_pay_vehicle` FOREIGN KEY (`vehicle_id`) REFERENCES `vehicle` (`vehicle_id`)
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment`
--

LOCK TABLES `payment` WRITE;
/*!40000 ALTER TABLE `payment` DISABLE KEYS */;
INSERT INTO `payment` VALUES (1,2,NULL,NULL,0,2400,'PAY','READY','PARKING',NULL,'301e3fd7-9f06-4473-94ef-12fdbe4fab2c',NULL,'2026-05-06 00:58:48',0),(2,2,NULL,NULL,2400,2400,'PAY','SUCCESS','PARKING',NULL,'tgen_20260506095909cVR59','2026-05-06 09:59:16','2026-05-06 00:58:50',0),(3,3,NULL,NULL,2400,2400,'PAY','SUCCESS','PARKING',NULL,'tgen_20260506100233bYv79','2026-05-06 10:02:42','2026-05-06 01:02:13',0),(4,NULL,NULL,4,100000,100000,'PAY','REFUNDED','SUBSCRIPTION',NULL,'a2ab07a5-f187-4cea-8c36-947d5ad7daad','2026-05-06 10:11:22','2026-05-06 01:11:22',73333),(5,5,NULL,NULL,3800,3800,'PAY','SUCCESS','PARKING',NULL,'tgen_20260506101903aSx02','2026-05-06 10:19:14','2026-05-06 01:18:59',0),(6,7,NULL,NULL,0,7300,'PAY','READY','PARKING',NULL,'28aa9e97-6ede-4b3b-a178-81181a107025',NULL,'2026-05-06 03:05:05',0),(7,7,NULL,NULL,0,7300,'PAY','READY','PARKING',NULL,'0fe519eb-1d89-4f95-9735-ab88d51ed923',NULL,'2026-05-06 03:05:07',0),(8,7,NULL,NULL,0,8000,'PAY','READY','PARKING',NULL,'222badc0-4054-4605-8ca8-53b01ae9705c',NULL,'2026-05-06 03:13:18',0),(9,7,NULL,NULL,0,10800,'PAY','READY','PARKING',NULL,'d0afb2f1-17c2-4a23-9660-88d8039e92b4',NULL,'2026-05-06 03:59:39',0),(10,2,NULL,NULL,12600,12600,'PAY','SUCCESS','PARKING',NULL,'tgen_202605061259446gJx7','2026-05-06 12:59:54','2026-05-06 03:59:43',0),(11,NULL,NULL,2,100000,100000,'PAY','CANCELLED','SUBSCRIPTION',NULL,'ae29e3d9-22cb-4645-bceb-7cb29e6ce424','2026-05-06 13:01:39','2026-05-06 04:01:39',100000),(12,7,NULL,NULL,11500,11500,'PAY','SUCCESS','PARKING',NULL,'tgen_20260506130934a6y24','2026-05-06 13:09:42','2026-05-06 04:09:31',0),(13,3,NULL,NULL,0,12600,'PAY','READY','PARKING',NULL,'dd41fc3f-36d6-474d-9417-3bfa7319ad3f',NULL,'2026-05-06 04:11:19',0),(14,3,NULL,NULL,0,12600,'PAY','READY','PARKING',NULL,'39029460-7a5d-44f5-99a4-4c619c670444',NULL,'2026-05-06 04:11:33',0),(15,10,NULL,NULL,5200,5200,'PAY','SUCCESS','PARKING',NULL,'tgen_202605061314173Okk0','2026-05-06 13:14:24','2026-05-06 04:14:14',0),(16,4,NULL,NULL,0,15000,'PAY','READY','PARKING',NULL,'c830e6f2-02da-4107-b7c0-24d223f2eaf1',NULL,'2026-05-06 04:15:09',0),(17,3,NULL,NULL,0,12600,'PAY','READY','PARKING',NULL,'c0966788-aca2-4b3d-9d7e-f0508f3e5c15',NULL,'2026-05-06 05:11:41',0),(18,3,NULL,NULL,0,12600,'PAY','READY','PARKING',NULL,'1480a699-fb8f-4460-8286-a04fb592e09d',NULL,'2026-05-06 05:11:42',0),(19,3,NULL,NULL,0,12600,'PAY','READY','PARKING',NULL,'5b302efa-5fbf-43c9-96a8-baace90fe45f',NULL,'2026-05-06 05:12:07',0),(20,10,NULL,NULL,4200,4200,'PAY','SUCCESS','PARKING',NULL,'tgen_20260506141318abmE8','2026-05-06 14:13:25','2026-05-06 05:13:17',0),(21,3,NULL,NULL,0,12600,'PAY','READY','PARKING',NULL,'5cd7ec8c-f9aa-46d3-af50-5a47b90bf905',NULL,'2026-05-06 05:14:52',0),(22,3,NULL,NULL,2600,2600,'PAY','SUCCESS','PARKING',NULL,'tgen_202605061435252rs71','2026-05-06 14:35:35','2026-05-06 05:35:19',0),(23,16,NULL,NULL,2400,2400,'PAY','SUCCESS','PARKING',NULL,'tgen_202605061437482rDm8','2026-05-06 14:37:57','2026-05-06 05:37:46',0),(24,17,NULL,NULL,0,2400,'PAY','READY','PARKING',NULL,'dd76d0f9-b47a-4497-af54-87eacde6643b',NULL,'2026-05-06 05:38:48',0),(25,NULL,NULL,2,100000,100000,'PAY','CANCELLED','SUBSCRIPTION',NULL,'5965661a-f8f0-450f-8ef5-6a810f47f750','2026-05-06 14:58:33','2026-05-06 05:58:33',100000),(26,30,NULL,NULL,54000,54000,'PAY','SUCCESS','PARKING',NULL,'tgen_20260514155702Jpwo8','2026-05-14 15:57:10','2026-05-14 06:56:59',0),(27,33,NULL,4,1334,1334,'POINT','SUCCESS','PARKING',NULL,'tgen_20260515160131t5dP7','2026-05-15 16:01:38','2026-05-15 16:01:28',0),(28,33,NULL,4,11666,11666,'PAY','SUCCESS','PARKING',NULL,'tgen_20260515160131t5dP7','2026-05-15 16:01:38','2026-05-15 16:01:28',0),(29,33,NULL,4,75000,75000,'PAY','SUCCESS','PARKING',NULL,'tgen_20260520152442rlaV1','2026-05-20 15:24:51','2026-05-20 15:24:40',0),(30,30,NULL,NULL,86800,86800,'PAY','SUCCESS','PARKING',NULL,'tgen_20260520152533HtFS2','2026-05-20 15:25:41','2026-05-20 15:25:32',0),(31,33,NULL,4,0,0,'FREE_POLICY','SUCCESS','PARKING',NULL,'POINT_FULL_PAYMENT','2026-05-20 16:22:14','2026-05-20 16:22:14',0),(32,30,NULL,NULL,4800,4800,'PAY','SUCCESS','PARKING',NULL,'tgen_20260520162237TRTh9','2026-05-20 16:22:43','2026-05-20 16:22:35',0),(33,30,NULL,NULL,4400,4400,'PAY','SUCCESS','PARKING',NULL,'tgen_20260520172119N6jZ3','2026-05-20 17:21:28','2026-05-20 17:21:17',0);
/*!40000 ALTER TABLE `payment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `point_log`
--

DROP TABLE IF EXISTS `point_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `point_log` (
  `point_log_id` bigint NOT NULL AUTO_INCREMENT,
  `payment_id` bigint DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `change_amount` int NOT NULL,
  `before_point` int NOT NULL,
  `after_point` int NOT NULL,
  `reason` enum('PAYMENT_EARN','PAYMENT_USE','REFUND','ADMIN_GRANT','ADMIN_REVOKE') NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`point_log_id`),
  KEY `fk_pl_user` (`user_id`),
  KEY `fk_pl_payment` (`payment_id`),
  CONSTRAINT `fk_pl_payment` FOREIGN KEY (`payment_id`) REFERENCES `payment` (`payment_id`),
  CONSTRAINT `fk_pl_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `point_log`
--

LOCK TABLES `point_log` WRITE;
/*!40000 ALTER TABLE `point_log` DISABLE KEYS */;
INSERT INTO `point_log` VALUES (1,4,7,5000,0,5000,'PAYMENT_EARN','정기권 구매 포인트 적립','2026-05-06 01:11:22'),(2,11,6,5000,0,5000,'PAYMENT_EARN','정기권 구매 포인트 적립','2026-05-06 04:01:40'),(3,11,6,-5000,5000,0,'REFUND','정기권 환불 - 적립 포인트 회수','2026-05-06 05:14:16'),(4,25,9,5000,0,5000,'PAYMENT_EARN','정기권 구매 포인트 적립','2026-05-06 05:58:33'),(5,25,9,-5000,5000,0,'REFUND','정기권 환불 - 적립 포인트 회수','2026-05-06 05:58:47'),(6,4,7,-3666,5000,1334,'REFUND','정기권 환불 - 적립 포인트 회수','2026-05-14 08:04:13'),(7,27,7,1334,1334,0,'PAYMENT_USE','결제 시 포인트 사용','2026-05-15 16:01:38'),(8,29,7,3750,0,3750,'PAYMENT_EARN','결제 포인트 적립','2026-05-20 15:24:51');
/*!40000 ALTER TABLE `point_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `report`
--

DROP TABLE IF EXISTS `report`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `report` (
  `report_id` bigint NOT NULL AUTO_INCREMENT,
  `reporter_user_id` bigint NOT NULL COMMENT '신고를 한 유저 ID',
  `vehicle_id` bigint DEFAULT NULL COMMENT '신고 당한 차량 ID (차량 번호로 추후 매칭 가능)',
  `car_number` varchar(25) NOT NULL COMMENT '신고 대상 차량 번호',
  `report_type` enum('DOUBLE_PARK','BLOCKING','NOISE','ILLEGAL_PARKING','OTHER') NOT NULL COMMENT '신고 유형',
  `description` varchar(255) DEFAULT NULL,
  `image_url` varchar(512) DEFAULT NULL COMMENT '증거 사진 URL',
  `status` enum('PENDING','APPROVED','REJECTED','CANCELLED') NOT NULL DEFAULT 'PENDING' COMMENT '신고 처리 상태',
  `admin_id` bigint DEFAULT NULL COMMENT '해당 신고를 처리한 관리자 ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '신고 접수 일시',
  `resolved_at` datetime DEFAULT NULL COMMENT '관리자 처리 완료 일시',
  PRIMARY KEY (`report_id`),
  KEY `fk_rep_vehicle` (`vehicle_id`),
  KEY `fk_rep_admin` (`admin_id`),
  KEY `idx_rep_reporter_status` (`reporter_user_id`,`status`),
  KEY `idx_rep_status` (`status`),
  KEY `idx_rep_car_number` (`car_number`),
  CONSTRAINT `fk_rep_admin` FOREIGN KEY (`admin_id`) REFERENCES `admin` (`admin_id`),
  CONSTRAINT `fk_rep_reporter` FOREIGN KEY (`reporter_user_id`) REFERENCES `user` (`user_id`),
  CONSTRAINT `fk_rep_vehicle` FOREIGN KEY (`vehicle_id`) REFERENCES `vehicle` (`vehicle_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `report`
--

LOCK TABLES `report` WRITE;
/*!40000 ALTER TABLE `report` DISABLE KEYS */;
INSERT INTO `report` VALUES (1,3,NULL,'10나1296','ILLEGAL_PARKING','불법주차','https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260506_100315_4abe1d5e8472402c97b9f6262b19082b.jpg','APPROVED',2,'2026-05-06 10:03:16','2026-05-06 14:42:21'),(2,6,NULL,'11가1111','ILLEGAL_PARKING','주차자리 두개씀 ','https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260506_100315_b54a6d7942a24d2ca34fb658fc0f2940.jpg','APPROVED',1,'2026-05-06 10:03:16','2026-05-15 13:40:11'),(3,5,NULL,'111가1111','ILLEGAL_PARKING','ㅅ','https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260506_103116_7ee7faa744474f6b9243ad47b98c70e2.jpg','REJECTED',5,'2026-05-06 10:31:17','2026-05-06 13:02:16'),(4,8,NULL,'69도3445','DOUBLE_PARK','이중주차예요','https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260506_103826_3899717cdf3e4c9e8160547a4a6f4e67.jpg','REJECTED',5,'2026-05-06 10:38:27','2026-05-06 13:01:56'),(5,5,NULL,'111가0123','ILLEGAL_PARKING','테스트','https://parking-system-storage.s3.ap-northeast-2.amazonaws.com/20260506_150444_391fef21ca6749ebb6b61663eb588d1e.jpg','APPROVED',1,'2026-05-06 15:04:45','2026-05-08 14:10:53');
/*!40000 ALTER TABLE `report` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reservation`
--

DROP TABLE IF EXISTS `reservation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reservation` (
  `reservation_id` bigint NOT NULL AUTO_INCREMENT,
  `vehicle_id` bigint DEFAULT NULL COMMENT '방문 차량 ID (등록된 차량일 경우)',
  `user_id` bigint NOT NULL COMMENT '신청한 입주민 ID',
  `car_number` varchar(255) NOT NULL,
  `visit_start_at` datetime NOT NULL COMMENT '입차 허용 시작 시간',
  `visit_end_at` datetime NOT NULL COMMENT '예약 종료(출차 권장) 시간',
  `status` enum('PENDING','REJECTED','RESERVED','ENTERED','NO_SHOW','CANCELLED','COMPLETED') NOT NULL DEFAULT 'PENDING',
  `purpose` enum('FAMILY','FRIEND','BUSINESS','DELIVERY','OTHER') NOT NULL,
  `actual_entry_at` datetime DEFAULT NULL,
  `is_free` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `cancelled_at` datetime DEFAULT NULL COMMENT '취소 버튼을 누른 시각',
  PRIMARY KEY (`reservation_id`),
  KEY `fk_res_vehicle` (`vehicle_id`),
  KEY `idx_res_car_status` (`car_number`,`status`),
  KEY `idx_res_user_status` (`user_id`,`status`),
  KEY `idx_res_status_end` (`status`,`visit_end_at`),
  KEY `idx_res_created_status` (`created_at`,`status`),
  CONSTRAINT `fk_res_host` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`),
  CONSTRAINT `fk_res_vehicle` FOREIGN KEY (`vehicle_id`) REFERENCES `vehicle` (`vehicle_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reservation`
--

LOCK TABLES `reservation` WRITE;
/*!40000 ALTER TABLE `reservation` DISABLE KEYS */;
INSERT INTO `reservation` VALUES (1,NULL,3,'154러7070','2026-05-07 00:00:00','2026-05-07 01:00:00','NO_SHOW','FAMILY',NULL,1,'2026-05-06 00:43:22',NULL),(2,NULL,5,'000가0000','2026-05-07 11:00:00','2026-05-07 12:00:00','CANCELLED','OTHER',NULL,1,'2026-05-06 01:20:30','2026-05-06 10:27:30'),(3,NULL,5,'65라4088','2026-05-07 11:00:00','2026-05-07 12:00:00','CANCELLED','OTHER',NULL,1,'2026-05-06 01:29:11','2026-05-06 13:16:09'),(4,NULL,6,'11나1234','2026-05-07 14:00:00','2026-05-07 15:00:00','CANCELLED','FAMILY',NULL,1,'2026-05-06 01:31:36',NULL),(5,NULL,6,'12구1234','2026-05-07 15:00:00','2026-05-07 16:00:00','CANCELLED','FRIEND',NULL,1,'2026-05-06 03:55:47','2026-05-06 12:55:55'),(6,NULL,9,'11가1234','2026-05-08 16:00:00','2026-05-08 17:00:00','CANCELLED','FAMILY',NULL,1,'2026-05-06 05:59:15','2026-05-06 15:00:09'),(7,NULL,9,'11가1234','2026-05-07 14:00:00','2026-05-07 15:00:00','REJECTED','FRIEND',NULL,1,'2026-05-06 06:00:44','2026-05-06 15:01:00'),(8,NULL,4,'12가4568','2026-05-11 11:00:00','2026-05-11 12:00:00','NO_SHOW','FRIEND',NULL,1,'2026-05-10 08:06:26',NULL);
/*!40000 ALTER TABLE `reservation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reservation_event_policy`
--

DROP TABLE IF EXISTS `reservation_event_policy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reservation_event_policy` (
  `reservation_event_policy_id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint NOT NULL COMMENT '정책을 설정한 관리자 ID',
  `event_name` varchar(200) NOT NULL,
  `start_date` datetime NOT NULL,
  `end_date` datetime DEFAULT NULL COMMENT '정책 종료 일시 (NULL이면 무기한 적용)',
  `daily_limit_per_household` int DEFAULT NULL COMMENT '세대별 일일 예약 가능 횟수 (NULL이면 무제한)',
  `monthly_limit_per_household` int DEFAULT NULL COMMENT '세대별 월간 총 예약 가능 횟수 (NULL이면 무제한)',
  `max_active_reservations` int NOT NULL DEFAULT '1' COMMENT '동시에 보유 가능한 활성 예약 수',
  `permitted_minutes` int NOT NULL DEFAULT '60' COMMENT '방문 예약 시 부여되는 주차 허용 시간(분 단위)',
  `no_show_penalty_enabled` tinyint(1) NOT NULL DEFAULT '0' COMMENT '노쇼 발생 시 페널티 여부',
  PRIMARY KEY (`reservation_event_policy_id`),
  KEY `fk_rep_admin_link` (`admin_id`),
  CONSTRAINT `fk_rep_admin_link` FOREIGN KEY (`admin_id`) REFERENCES `admin` (`admin_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reservation_event_policy`
--

LOCK TABLES `reservation_event_policy` WRITE;
/*!40000 ALTER TABLE `reservation_event_policy` DISABLE KEYS */;
INSERT INTO `reservation_event_policy` VALUES (1,1,'방문예약 정책','2026-05-06 09:41:00','2026-05-06 14:50:59',5,10,1,60,1),(2,2,'새로운정책','2026-05-06 14:51:00',NULL,10,100,2,60,1);
/*!40000 ALTER TABLE `reservation_event_policy` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `social_account`
--

DROP TABLE IF EXISTS `social_account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `social_account` (
  `social_account_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `provider` enum('LOCAL','KAKAO','NAVER') NOT NULL,
  `provider_id` varchar(150) NOT NULL,
  `connected_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`social_account_id`),
  UNIQUE KEY `uq_provider` (`provider`,`provider_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `social_account_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `social_account`
--

LOCK TABLES `social_account` WRITE;
/*!40000 ALTER TABLE `social_account` DISABLE KEYS */;
INSERT INTO `social_account` VALUES (1,3,'NAVER','dsbsWuHQTmQEqCq6B4nyHvCujaElEg6IcbhjlmC_wUY','2026-05-06 09:37:28'),(6,6,'NAVER','lTA5WX_58tWzE8HncKat11NAyEOrOFqVHJ7KqH8ftYo','2026-05-06 17:25:44');
/*!40000 ALTER TABLE `social_account` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `store`
--

DROP TABLE IF EXISTS `store`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `store` (
  `store_id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `location` varchar(255) DEFAULT NULL,
  `status` enum('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
  `terminal_password` varchar(255) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` datetime DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  PRIMARY KEY (`store_id`),
  KEY `fk_store_created_by` (`created_by`),
  KEY `fk_store_updated_by` (`updated_by`),
  KEY `idx_store_status_name` (`status`,`name`),
  CONSTRAINT `fk_store_created_by` FOREIGN KEY (`created_by`) REFERENCES `admin` (`admin_id`),
  CONSTRAINT `fk_store_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `admin` (`admin_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `store`
--

LOCK TABLES `store` WRITE;
/*!40000 ALTER TABLE `store` DISABLE KEYS */;
INSERT INTO `store` VALUES (1,'관리자','1층 101호','ACTIVE','123456','2026-05-05 07:45:06','2026-05-05 16:53:58',NULL,NULL,2),(2,'1층 편의점','1층 102호','ACTIVE','456789','2026-05-05 07:45:06','2026-05-06 00:49:44',NULL,NULL,1),(3,'1층 약국','1층 103호','INACTIVE','969696','2026-05-05 07:45:06','2026-05-06 05:50:19','2026-05-06 14:50:19',NULL,2),(4,'2층 식당','2층 201호','INACTIVE','1234','2026-05-05 07:45:06',NULL,NULL,NULL,NULL),(5,'2층 미용실','2층 202호','INACTIVE','1234','2026-05-05 07:45:06',NULL,NULL,NULL,NULL),(6,'2층 세탁소','2층 203호','INACTIVE','1234','2026-05-05 07:45:06',NULL,NULL,NULL,NULL),(7,'3층 학원','3층 301호','INACTIVE','1234','2026-05-05 07:45:06',NULL,NULL,NULL,NULL),(8,'3층 치과','3층 302호','INACTIVE','1234','2026-05-05 07:45:06',NULL,NULL,NULL,NULL),(9,'3층 부동산','3층 303호','INACTIVE','1234','2026-05-05 07:45:06',NULL,NULL,NULL,NULL),(10,'4층 피부과','4층 401호','INACTIVE','1234','2026-05-05 07:45:06',NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `store` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `store_ticket_config`
--

DROP TABLE IF EXISTS `store_ticket_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `store_ticket_config` (
  `store_ticket_config_id` bigint NOT NULL AUTO_INCREMENT,
  `store_id` bigint NOT NULL,
  `ticket_policy_id` bigint NOT NULL,
  `monthly_quota` int NOT NULL DEFAULT '0',
  `last_issued_at` datetime DEFAULT NULL,
  PRIMARY KEY (`store_ticket_config_id`),
  UNIQUE KEY `uq_store_policy` (`store_id`,`ticket_policy_id`),
  KEY `fk_stc_ticket_policy` (`ticket_policy_id`),
  KEY `idx_stc_store_id` (`store_id`),
  CONSTRAINT `fk_stc_store` FOREIGN KEY (`store_id`) REFERENCES `store` (`store_id`),
  CONSTRAINT `fk_stc_ticket_policy` FOREIGN KEY (`ticket_policy_id`) REFERENCES `ticket_policy` (`ticket_policy_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `store_ticket_config`
--

LOCK TABLES `store_ticket_config` WRITE;
/*!40000 ALTER TABLE `store_ticket_config` DISABLE KEYS */;
INSERT INTO `store_ticket_config` VALUES (1,2,6,60,'2026-06-05 10:28:34');
/*!40000 ALTER TABLE `store_ticket_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `store_ticket_transaction`
--

DROP TABLE IF EXISTS `store_ticket_transaction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `store_ticket_transaction` (
  `transaction_id` bigint NOT NULL AUTO_INCREMENT,
  `wallet_id` bigint NOT NULL,
  `store_id` bigint NOT NULL,
  `ticket_policy_id` bigint NOT NULL,
  `transaction_type` enum('ISSUE','PURCHASE','USE','EXPIRE','ADJUST','CANCEL') NOT NULL,
  `quantity` int NOT NULL,
  `before_balance` int NOT NULL,
  `after_balance` int NOT NULL,
  `reference_type` enum('PARKING_LOG','PAYMENT','ADMIN','SYSTEM') NOT NULL,
  `reference_id` bigint DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_by_type` enum('ADMIN','STORE','SYSTEM') NOT NULL,
  `created_by_id` bigint DEFAULT NULL,
  PRIMARY KEY (`transaction_id`),
  KEY `fk_stt_wallet` (`wallet_id`),
  KEY `fk_stt_store` (`store_id`),
  KEY `fk_stt_policy` (`ticket_policy_id`),
  KEY `idx_stt_type_created` (`transaction_type`,`created_at`),
  CONSTRAINT `fk_stt_policy` FOREIGN KEY (`ticket_policy_id`) REFERENCES `ticket_policy` (`ticket_policy_id`),
  CONSTRAINT `fk_stt_store` FOREIGN KEY (`store_id`) REFERENCES `store` (`store_id`),
  CONSTRAINT `fk_stt_wallet` FOREIGN KEY (`wallet_id`) REFERENCES `store_ticket_wallet` (`wallet_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `store_ticket_transaction`
--

LOCK TABLES `store_ticket_transaction` WRITE;
/*!40000 ALTER TABLE `store_ticket_transaction` DISABLE KEYS */;
INSERT INTO `store_ticket_transaction` VALUES (1,1,2,6,'ISSUE',60,0,60,'SYSTEM',NULL,'2026-05-06 01:28:33','SYSTEM',NULL),(2,1,2,6,'USE',5,60,55,'PARKING_LOG',3,'2026-05-06 05:34:41','STORE',2),(3,1,2,6,'USE',20,55,35,'PARKING_LOG',23,'2026-05-06 05:39:07','STORE',2),(4,2,2,5,'PURCHASE',45,0,45,'PAYMENT',NULL,'2026-05-06 05:39:43','STORE',2),(5,3,3,6,'ISSUE',100,0,100,'SYSTEM',NULL,'2026-05-06 05:49:41','SYSTEM',NULL),(6,2,2,5,'PURCHASE',10,45,55,'PAYMENT',NULL,'2026-05-14 08:09:05','STORE',2),(7,1,2,6,'USE',1,35,34,'PARKING_LOG',33,'2026-05-14 08:09:13','STORE',2),(8,2,2,5,'USE',5,55,50,'PARKING_LOG',33,'2026-05-20 16:17:18','STORE',2),(9,1,2,6,'ISSUE',60,34,94,'SYSTEM',NULL,'2026-06-05 10:28:34','SYSTEM',NULL);
/*!40000 ALTER TABLE `store_ticket_transaction` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `store_ticket_wallet`
--

DROP TABLE IF EXISTS `store_ticket_wallet`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `store_ticket_wallet` (
  `wallet_id` bigint NOT NULL AUTO_INCREMENT,
  `store_id` bigint NOT NULL,
  `ticket_policy_id` bigint NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `issued_count` int NOT NULL DEFAULT '0',
  `used_count` int NOT NULL DEFAULT '0',
  `remaining_count` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`wallet_id`),
  KEY `fk_stw_policy` (`ticket_policy_id`),
  KEY `idx_stw_store_policy` (`store_id`,`ticket_policy_id`),
  CONSTRAINT `fk_stw_policy` FOREIGN KEY (`ticket_policy_id`) REFERENCES `ticket_policy` (`ticket_policy_id`),
  CONSTRAINT `fk_stw_store` FOREIGN KEY (`store_id`) REFERENCES `store` (`store_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `store_ticket_wallet`
--

LOCK TABLES `store_ticket_wallet` WRITE;
/*!40000 ALTER TABLE `store_ticket_wallet` DISABLE KEYS */;
INSERT INTO `store_ticket_wallet` VALUES (1,2,6,'2026-05-06 01:28:33',120,26,94),(2,2,5,'2026-05-06 05:39:43',55,5,50),(3,3,6,'2026-05-06 05:49:41',0,0,0);
/*!40000 ALTER TABLE `store_ticket_wallet` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `subscription`
--

DROP TABLE IF EXISTS `subscription`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subscription` (
  `subscription_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `vehicle_id` bigint DEFAULT NULL,
  `start_date` datetime NOT NULL,
  `end_date` datetime NOT NULL,
  `status` enum('ACTIVE','EXPIRED','CANCELLED','REFUNDED') NOT NULL,
  `payment_id` bigint NOT NULL,
  `price` int NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `activated_at` datetime DEFAULT NULL,
  `cancelled_at` datetime DEFAULT NULL,
  PRIMARY KEY (`subscription_id`),
  KEY `fk_sub_payment` (`payment_id`),
  KEY `idx_sub_vehicle_status_end` (`vehicle_id`,`status`,`end_date`),
  KEY `idx_sub_user_status` (`user_id`,`status`),
  KEY `idx_sub_status_end` (`status`,`end_date`),
  CONSTRAINT `fk_sub_payment` FOREIGN KEY (`payment_id`) REFERENCES `payment` (`payment_id`),
  CONSTRAINT `fk_sub_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`),
  CONSTRAINT `fk_sub_vehicle` FOREIGN KEY (`vehicle_id`) REFERENCES `vehicle` (`vehicle_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subscription`
--

LOCK TABLES `subscription` WRITE;
/*!40000 ALTER TABLE `subscription` DISABLE KEYS */;
INSERT INTO `subscription` VALUES (1,7,4,'2026-05-07 00:00:00','2026-06-06 00:00:00','REFUNDED',4,100000,'2026-05-06 01:11:22','2026-05-06 10:11:22','2026-05-14 17:04:13'),(2,6,2,'2026-05-07 00:00:00','2026-06-06 00:00:00','REFUNDED',11,100000,'2026-05-06 04:01:39','2026-05-06 13:01:39','2026-05-06 14:14:16'),(3,9,2,'2026-05-07 00:00:00','2026-06-06 00:00:00','REFUNDED',25,100000,'2026-05-06 05:58:33','2026-05-06 14:58:33','2026-05-06 14:58:47');
/*!40000 ALTER TABLE `subscription` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `system_setting`
--

DROP TABLE IF EXISTS `system_setting`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `system_setting` (
  `setting_key` varchar(150) NOT NULL,
  `setting_value` text NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `is_editable` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`setting_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `system_setting`
--

LOCK TABLES `system_setting` WRITE;
/*!40000 ALTER TABLE `system_setting` DISABLE KEYS */;
INSERT INTO `system_setting` VALUES ('DETECTED_CANCEL_MINUTES','1','미입차 자동 취소 대기 시간(분)',1),('ENTRY_LOCK','mutex','입차 트랜잭션 LOCK 키',0),('MIN_USAGE_POINT','100','포인트 최소 사용 단위',1),('OVERTIME_MIN_FEE','100','       ',1),('PAYMENT_POINT_EARN_RATE','5','결제 금액 대비 포인트 적립율(%)',1),('PAYMENT_VALID_MINUTES','5','결제 유효 시간(분)',1),('PG_COMMISSION_RATE','0.033','PG ',1),('POST_PAYMENT_GRACE_MINUTES','5','정산 후 무료 출차 허용 시간(분)',1),('REPORT_BLACKLIST_THRESHOLD','10','     ',1),('SUB_DURATION_DAYS','30','   ()',1),('SUB_MAX_COUNT','10','    ()',1),('SUB_MONTHLY_PRICE','100000',' 30  ',1),('TOTAL_DAILY_RESERVATION_LIMIT','10','        ',1),('VEHICLE_APPROVAL_EXPIRE_HOURS','72','차량 등록 승인 만료 시간(시간)',1),('VEHICLE_AUTO_APPROVAL_THRESHOLD','95','    (%)',1);
/*!40000 ALTER TABLE `system_setting` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ticket_policy`
--

DROP TABLE IF EXISTS `ticket_policy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ticket_policy` (
  `ticket_policy_id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `price` int NOT NULL DEFAULT '0',
  `discount_type` enum('TIME','AMOUNT','FREE','RATE') NOT NULL,
  `discount_value` int NOT NULL,
  `use_type` enum('STORE','ADMIN') NOT NULL DEFAULT 'STORE' COMMENT '정책 사용 주체 (상가용 또는 관리자 직접 할인용)',
  `max_discount_amount` int DEFAULT NULL,
  `valid_minutes` int DEFAULT NULL,
  `valid_days` int DEFAULT NULL,
  `stackable` tinyint(1) NOT NULL DEFAULT '1',
  `status` enum('ACTIVE','INACTIVE','DELETED') NOT NULL DEFAULT 'ACTIVE',
  `is_free_ticket` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ticket_policy_id`),
  KEY `idx_tp_use_status` (`use_type`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ticket_policy`
--

LOCK TABLES `ticket_policy` WRITE;
/*!40000 ALTER TABLE `ticket_policy` DISABLE KEYS */;
INSERT INTO `ticket_policy` VALUES (4,'관리자 1시간 할인','관리자 직권 1시간 무료',0,'TIME',60,'ADMIN',NULL,NULL,NULL,0,'ACTIVE',0,'2026-05-05 07:47:10'),(5,'[상가] 2시간 할인권','상가 이용객 2시간 할인권 ',2000,'TIME',120,'STORE',NULL,0,30,1,'ACTIVE',0,'2026-05-05 12:20:42'),(6,'[관리자] 2천원 할인권','상가 무료지급 할인권',0,'AMOUNT',2000,'ADMIN',NULL,0,30,1,'ACTIVE',1,'2026-05-05 12:21:40'),(7,'[관리자] 1천원 할인권','관리자 조정: 1천원 할인',0,'AMOUNT',1000,'ADMIN',NULL,0,30,1,'ACTIVE',0,'2026-05-05 12:22:23');
/*!40000 ALTER TABLE `ticket_policy` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `user_id` bigint NOT NULL AUTO_INCREMENT,
  `household_id` bigint DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `name` varchar(100) NOT NULL,
  `birth` date NOT NULL,
  `phone` varchar(30) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `status` enum('ACTIVE','DELETED') NOT NULL DEFAULT 'ACTIVE',
  `deleted_at` datetime DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `phone` (`phone`),
  KEY `fk_user_household` (`household_id`),
  CONSTRAINT `fk_user_household` FOREIGN KEY (`household_id`) REFERENCES `household` (`household_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,NULL,'$2a$10$qn4/.5wHMYLPBbZM2/Sx2Omxda1CHTaZ9tfnhnV7WO4ZHOP80u3Iq','test0101@test.com','test','2000-11-11','010-2123-5654','2026-05-05 16:48:59','2026-05-05 16:48:59','ACTIVE',NULL),(3,2,NULL,'dbsekdgh07@naver.com','윤상호','1994-09-07','010-3793-6855','2026-05-06 09:37:28','2026-05-06 09:42:45','ACTIVE',NULL),(4,20,'$2a$10$g4H39tzr.EwfZ8V1WEOhdeRVXS/8thpNvBzS.4ztWb0/Ftbbub33W','qhzud1207@gmail.com','김보경','2002-02-02','010-1234-5567','2026-05-06 09:37:33','2026-05-06 09:40:52','ACTIVE',NULL),(5,3,'$2a$10$6GEDtcvtiP7TjnsddQBFF.oowVDjzYBzcWgXHp.JyhsbSsEObyuXG','test@test.com','이윤진','1981-02-09','010-1234-1234','2026-05-06 09:54:47','2026-05-06 10:19:53','ACTIVE',NULL),(6,NULL,'$2a$10$ARA4.iK1/GknSrfO24xSPeUpP6wo/ExUUpxesMwELDTDEeOJmHY56','ydg5789@naver.com','유승원','1995-12-02','010-8218-5789','2026-05-06 09:57:16','2026-05-06 14:56:11','ACTIVE',NULL),(7,NULL,'$2a$10$mU0qGWNm/yNUwXSjlRXFM.S90oygBX3RBVnvC119knMakaycJPidi','chlwndus120@gmail.com','최주연','1997-10-26','010-2390-5481','2026-05-06 10:08:25','2026-05-06 10:08:25','ACTIVE',NULL),(8,NULL,'$2a$10$LGybB2059G.N5HSFFo43GeB6fD6vFLmISTJCezComKYD0PIUPZZKW','test@test.net','lee','1981-02-09','010-0000-0000','2026-05-06 10:37:48','2026-05-06 10:37:48','ACTIVE',NULL),(9,NULL,'$2a$10$QqRh2D.16BoepqJpbiz/COf6twvE9enizieS.N9Hq1QqkC2.FKWgW','hgd1234@test.com','홍길동','1980-01-01','010-1111-1234','2026-05-06 14:52:47','2026-05-06 15:01:00','ACTIVE',NULL);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_point`
--

DROP TABLE IF EXISTS `user_point`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_point` (
  `user_id` bigint NOT NULL,
  `current_point` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`user_id`),
  CONSTRAINT `fk_up_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_point`
--

LOCK TABLES `user_point` WRITE;
/*!40000 ALTER TABLE `user_point` DISABLE KEYS */;
INSERT INTO `user_point` VALUES (6,0),(7,3750),(9,0);
/*!40000 ALTER TABLE `user_point` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vehicle`
--

DROP TABLE IF EXISTS `vehicle`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vehicle` (
  `vehicle_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint DEFAULT NULL,
  `vehicle_name` varchar(100) DEFAULT NULL,
  `car_number` varchar(25) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted_at` datetime DEFAULT NULL,
  `status` enum('ACTIVE','DELETED','PENDING') NOT NULL,
  PRIMARY KEY (`vehicle_id`),
  UNIQUE KEY `car_number` (`car_number`),
  UNIQUE KEY `uk_vehicle_car_number` (`car_number`),
  KEY `idx_veh_user_status` (`user_id`,`status`),
  KEY `idx_veh_status` (`status`),
  CONSTRAINT `fk_vehicle_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vehicle`
--

LOCK TABLES `vehicle` WRITE;
/*!40000 ALTER TABLE `vehicle` DISABLE KEYS */;
INSERT INTO `vehicle` VALUES (1,3,'현대소나타','123가4568','2026-05-06 09:39:14',NULL,'ACTIVE'),(2,9,'현대소나타','12거3456','2026-05-06 09:58:35',NULL,'ACTIVE'),(3,5,'현대소나타','69도3445','2026-05-06 10:10:32',NULL,'ACTIVE'),(4,7,'현대소나타','02로0202','2026-05-06 10:10:34',NULL,'ACTIVE');
/*!40000 ALTER TABLE `vehicle` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vehicle_blacklist`
--

DROP TABLE IF EXISTS `vehicle_blacklist`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vehicle_blacklist` (
  `vehicle_blacklist_id` bigint NOT NULL AUTO_INCREMENT,
  `vehicle_id` bigint DEFAULT NULL COMMENT '등록된 차량 ID (비회원일 경우 NULL)',
  `car_number` varchar(25) NOT NULL COMMENT '차단 대상 차량 번호 (필수)',
  `reason_type` enum('REPORT_ACCUMULATION','ILLEGAL_VEHICLE','USER_BLACKLIST','ADMIN_MANUAL','SYSTEM_BLOCK') NOT NULL,
  `reason_detail` text,
  `start_date` datetime NOT NULL COMMENT '차단 시작 일시',
  `end_date` datetime NOT NULL COMMENT '차단 종료 예정 일시 (3000년=무기한)',
  `status` enum('ACTIVE','RELEASED') NOT NULL DEFAULT 'ACTIVE',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '기록 생성 일시',
  `released_at` datetime DEFAULT NULL COMMENT '실제 차단 해제 일시',
  PRIMARY KEY (`vehicle_blacklist_id`),
  UNIQUE KEY `car_number` (`car_number`),
  KEY `fk_vbl_vehicle` (`vehicle_id`),
  KEY `idx_vbl_car_status` (`car_number`,`status`),
  CONSTRAINT `fk_vbl_vehicle` FOREIGN KEY (`vehicle_id`) REFERENCES `vehicle` (`vehicle_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vehicle_blacklist`
--

LOCK TABLES `vehicle_blacklist` WRITE;
/*!40000 ALTER TABLE `vehicle_blacklist` DISABLE KEYS */;
INSERT INTO `vehicle_blacklist` VALUES (1,NULL,'111가1111','ADMIN_MANUAL','상습 불법주차','2026-05-06 12:57:49','2026-05-06 12:58:15','RELEASED','2026-05-06 12:57:49','2026-05-06 12:58:15'),(2,NULL,'65노0887','ADMIN_MANUAL','ㅋ','2026-05-06 12:59:09','2026-05-06 13:05:39','RELEASED','2026-05-06 12:59:09','2026-05-06 13:05:39'),(4,NULL,'65노0888','USER_BLACKLIST','ㅁ','2026-05-06 13:07:26','2026-05-06 13:07:39','RELEASED','2026-05-06 13:07:26','2026-05-06 13:07:39'),(5,NULL,'65라4088','USER_BLACKLIST','ㅋ','2026-05-06 13:08:39','2026-05-06 13:16:18','RELEASED','2026-05-06 13:08:39','2026-05-06 13:16:18'),(6,NULL,'222가2222','USER_BLACKLIST','ㅁㅁ','2026-05-06 15:05:48','2026-05-06 15:06:06','RELEASED','2026-05-06 15:05:48','2026-05-06 15:06:06');
/*!40000 ALTER TABLE `vehicle_blacklist` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vehicle_report_stat`
--

DROP TABLE IF EXISTS `vehicle_report_stat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vehicle_report_stat` (
  `car_number` varchar(25) NOT NULL,
  `valid_report_count` int DEFAULT '0',
  `total_report_count` int DEFAULT '0',
  `last_reported_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`car_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vehicle_report_stat`
--

LOCK TABLES `vehicle_report_stat` WRITE;
/*!40000 ALTER TABLE `vehicle_report_stat` DISABLE KEYS */;
INSERT INTO `vehicle_report_stat` VALUES ('10나1296',1,1,'2026-05-06 10:03:16',NULL),('111가0123',1,1,'2026-05-06 15:04:45',NULL),('111가1111',0,1,'2026-05-06 10:31:17',NULL),('11가1111',1,1,'2026-05-06 10:03:16',NULL),('69도3445',0,1,'2026-05-06 10:38:27',NULL);
/*!40000 ALTER TABLE `vehicle_report_stat` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-11 11:52:50

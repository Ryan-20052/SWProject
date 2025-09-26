-- MySQL dump 10.13  Distrib 8.4.6, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: swproject
-- ------------------------------------------------------
-- Server version	8.4.6

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
-- Table structure for table `admin_log`
--

DROP TABLE IF EXISTS `admin_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `action` varchar(255) DEFAULT NULL,
  `table_affected` varchar(255) DEFAULT NULL,
  `time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin_log`
--

LOCK TABLES `admin_log` WRITE;
/*!40000 ALTER TABLE `admin_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `admin_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cart_item`
--

DROP TABLE IF EXISTS `cart_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `shopping_cart_id` bigint DEFAULT NULL,
  `amount` bigint DEFAULT NULL,
  `variant_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `shopping_cart_id` (`shopping_cart_id`),
  KEY `variant_id` (`variant_id`),
  CONSTRAINT `cart_item_ibfk_1` FOREIGN KEY (`shopping_cart_id`) REFERENCES `shopping_cart` (`id`),
  CONSTRAINT `cart_item_ibfk_2` FOREIGN KEY (`variant_id`) REFERENCES `variant` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cart_item`
--

LOCK TABLES `cart_item` WRITE;
/*!40000 ALTER TABLE `cart_item` DISABLE KEYS */;
/*!40000 ALTER TABLE `cart_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `categories`
--

DROP TABLE IF EXISTS `categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categories`
--

LOCK TABLES `categories` WRITE;
/*!40000 ALTER TABLE `categories` DISABLE KEYS */;
INSERT INTO `categories` VALUES (1,'Học tập','Phần mềm và công cụ hỗ trợ học tập'),(2,'Giải trí','Ứng dụng và game giải trí'),(3,'Làm việc','Công cụ phục vụ công việc, văn phòng'),(4,'Windows','Hệ điều hành và license Windows'),(5,'VPN','Phần mềm VPN, bảo mật kết nối');
/*!40000 ALTER TABLE `categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `customer`
--

DROP TABLE IF EXISTS `customer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customer` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customer`
--

LOCK TABLES `customer` WRITE;
/*!40000 ALTER TABLE `customer` DISABLE KEYS */;
/*!40000 ALTER TABLE `customer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `license_key`
--

DROP TABLE IF EXISTS `license_key`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `license_key` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(255) DEFAULT NULL,
  `order_detail_id` bigint DEFAULT NULL,
  `activated_at` datetime DEFAULT NULL,
  `expired_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `order_detail_id` (`order_detail_id`),
  CONSTRAINT `license_key_ibfk_1` FOREIGN KEY (`order_detail_id`) REFERENCES `orders_detail` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `license_key`
--

LOCK TABLES `license_key` WRITE;
/*!40000 ALTER TABLE `license_key` DISABLE KEYS */;
/*!40000 ALTER TABLE `license_key` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `manager`
--

DROP TABLE IF EXISTS `manager`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `manager` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_name` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `phone_number` varchar(20) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `manager`
--

LOCK TABLES `manager` WRITE;
/*!40000 ALTER TABLE `manager` DISABLE KEYS */;
INSERT INTO `manager` VALUES (1,'admin1','123456','Quản lý chính','0123456789','admin@example.com');
/*!40000 ALTER TABLE `manager` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notification`
--

DROP TABLE IF EXISTS `notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `customer_id` bigint DEFAULT NULL,
  `message` varchar(255) DEFAULT NULL,
  `sended_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `customer_id` (`customer_id`),
  CONSTRAINT `notification_ibfk_1` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notification`
--

LOCK TABLES `notification` WRITE;
/*!40000 ALTER TABLE `notification` DISABLE KEYS */;
/*!40000 ALTER TABLE `notification` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `customer_id` bigint DEFAULT NULL,
  `total_amount` bigint DEFAULT NULL,
  `order_date` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `customer_id` (`customer_id`),
  CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders_detail`
--

DROP TABLE IF EXISTS `orders_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `variant_id` bigint DEFAULT NULL,
  `amount` bigint DEFAULT NULL,
  `order_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `variant_id` (`variant_id`),
  KEY `order_id` (`order_id`),
  CONSTRAINT `orders_detail_ibfk_1` FOREIGN KEY (`variant_id`) REFERENCES `variant` (`id`),
  CONSTRAINT `orders_detail_ibfk_2` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders_detail`
--

LOCK TABLES `orders_detail` WRITE;
/*!40000 ALTER TABLE `orders_detail` DISABLE KEYS */;
/*!40000 ALTER TABLE `orders_detail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product`
--

DROP TABLE IF EXISTS `product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `categories_id` bigint DEFAULT NULL,
  `manager_id` bigint DEFAULT NULL,
  `category_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `categories_id` (`categories_id`),
  KEY `manager_id` (`manager_id`),
  CONSTRAINT `product_ibfk_1` FOREIGN KEY (`categories_id`) REFERENCES `categories` (`id`),
  CONSTRAINT `product_ibfk_2` FOREIGN KEY (`manager_id`) REFERENCES `manager` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=61 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product`
--

LOCK TABLES `product` WRITE;
/*!40000 ALTER TABLE `product` DISABLE KEYS */;
INSERT INTO `product` VALUES (41,'Microsoft Office 365',1,1,NULL),(42,'Grammarly Premium',1,1,NULL),(43,'Khan Academy Plus',1,1,NULL),(44,'Coursera Pro',1,1,NULL),(45,'Spotify Premium',2,1,NULL),(46,'Netflix Gift Card',2,1,NULL),(47,'Steam Wallet 100k',2,1,NULL),(48,'Disney+ 1 năm',2,1,NULL),(49,'Slack Pro',3,1,NULL),(50,'Zoom Business',3,1,NULL),(51,'Notion Plus',3,1,NULL),(52,'Trello Premium',3,1,NULL),(53,'Windows 10 Home Key',4,1,NULL),(54,'Windows 10 Pro Key',4,1,NULL),(55,'Windows 11 Home Key',4,1,NULL),(56,'Windows 11 Pro Key',4,1,NULL),(57,'NordVPN 1 năm',5,1,NULL),(58,'ExpressVPN 6 tháng',5,1,NULL),(59,'Surfshark VPN',5,1,NULL),(60,'CyberGhost VPN',5,1,NULL);
/*!40000 ALTER TABLE `product` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_img`
--

DROP TABLE IF EXISTS `product_img`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_img` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `product_id` bigint DEFAULT NULL,
  `img_url` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `product_id` (`product_id`),
  CONSTRAINT `product_img_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_img`
--

LOCK TABLES `product_img` WRITE;
/*!40000 ALTER TABLE `product_img` DISABLE KEYS */;
/*!40000 ALTER TABLE `product_img` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `id` int NOT NULL AUTO_INCREMENT,
  `category_id` int NOT NULL,
  `manager_id` int NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
/*!40000 ALTER TABLE `products` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `review`
--

DROP TABLE IF EXISTS `review`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `review` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `customer_id` bigint DEFAULT NULL,
  `order_id` bigint DEFAULT NULL,
  `rating` bigint DEFAULT NULL,
  `comment` varchar(255) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `customer_id` (`customer_id`),
  KEY `order_id` (`order_id`),
  CONSTRAINT `review_ibfk_1` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`id`),
  CONSTRAINT `review_ibfk_2` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `review`
--

LOCK TABLES `review` WRITE;
/*!40000 ALTER TABLE `review` DISABLE KEYS */;
/*!40000 ALTER TABLE `review` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shopping_cart`
--

DROP TABLE IF EXISTS `shopping_cart`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shopping_cart` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `customer_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `customer_id` (`customer_id`),
  CONSTRAINT `shopping_cart_ibfk_1` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shopping_cart`
--

LOCK TABLES `shopping_cart` WRITE;
/*!40000 ALTER TABLE `shopping_cart` DISABLE KEYS */;
/*!40000 ALTER TABLE `shopping_cart` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `password` varchar(255) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `variant`
--

DROP TABLE IF EXISTS `variant`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `variant` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `stock_quantity` bigint DEFAULT NULL,
  `duration` varchar(255) DEFAULT NULL,
  `product_id` bigint DEFAULT NULL,
  `price` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `product_id` (`product_id`),
  CONSTRAINT `variant_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=159 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `variant`
--

LOCK TABLES `variant` WRITE;
/*!40000 ALTER TABLE `variant` DISABLE KEYS */;
INSERT INTO `variant` VALUES (1,'Microsoft Office 365 - 1 tháng',100,'1 tháng',41,50000),(2,'Grammarly Premium - 1 tháng',100,'1 tháng',42,50000),(3,'Khan Academy Plus - 1 tháng',100,'1 tháng',43,50000),(4,'Coursera Pro - 1 tháng',100,'1 tháng',44,50000),(5,'Spotify Premium - 1 tháng',100,'1 tháng',45,50000),(6,'Netflix Gift Card - 1 tháng',100,'1 tháng',46,50000),(7,'Steam Wallet 100k - 1 tháng',100,'1 tháng',47,50000),(8,'Disney+ 1 năm - 1 tháng',100,'1 tháng',48,50000),(9,'Slack Pro - 1 tháng',100,'1 tháng',49,50000),(10,'Zoom Business - 1 tháng',100,'1 tháng',50,50000),(11,'Notion Plus - 1 tháng',100,'1 tháng',51,50000),(12,'Trello Premium - 1 tháng',100,'1 tháng',52,50000),(13,'Windows 10 Home Key - 1 tháng',100,'1 tháng',53,50000),(14,'Windows 10 Pro Key - 1 tháng',100,'1 tháng',54,50000),(15,'Windows 11 Home Key - 1 tháng',100,'1 tháng',55,50000),(16,'Windows 11 Pro Key - 1 tháng',100,'1 tháng',56,50000),(17,'NordVPN 1 năm - 1 tháng',100,'1 tháng',57,50000),(18,'ExpressVPN 6 tháng - 1 tháng',100,'1 tháng',58,50000),(19,'Surfshark VPN - 1 tháng',100,'1 tháng',59,50000),(20,'CyberGhost VPN - 1 tháng',100,'1 tháng',60,50000),(21,'Microsoft Office 365 - 3 tháng',100,'3 tháng',41,120000),(22,'Grammarly Premium - 3 tháng',100,'3 tháng',42,120000),(23,'Khan Academy Plus - 3 tháng',100,'3 tháng',43,120000),(24,'Coursera Pro - 3 tháng',100,'3 tháng',44,120000),(25,'Spotify Premium - 3 tháng',100,'3 tháng',45,120000),(26,'Netflix Gift Card - 3 tháng',100,'3 tháng',46,120000),(27,'Steam Wallet 100k - 3 tháng',100,'3 tháng',47,120000),(28,'Disney+ 1 năm - 3 tháng',100,'3 tháng',48,120000),(29,'Slack Pro - 3 tháng',100,'3 tháng',49,120000),(30,'Zoom Business - 3 tháng',100,'3 tháng',50,120000),(31,'Notion Plus - 3 tháng',100,'3 tháng',51,120000),(32,'Trello Premium - 3 tháng',100,'3 tháng',52,120000),(33,'Windows 10 Home Key - 3 tháng',100,'3 tháng',53,120000),(34,'Windows 10 Pro Key - 3 tháng',100,'3 tháng',54,120000),(35,'Windows 11 Home Key - 3 tháng',100,'3 tháng',55,120000),(36,'Windows 11 Pro Key - 3 tháng',100,'3 tháng',56,120000),(37,'NordVPN 1 năm - 3 tháng',100,'3 tháng',57,120000),(38,'ExpressVPN 6 tháng - 3 tháng',100,'3 tháng',58,120000),(39,'Surfshark VPN - 3 tháng',100,'3 tháng',59,120000),(40,'CyberGhost VPN - 3 tháng',100,'3 tháng',60,120000),(41,'Microsoft Office 365 - 9 tháng',100,'9 tháng',41,300000),(42,'Grammarly Premium - 9 tháng',100,'9 tháng',42,300000),(43,'Khan Academy Plus - 9 tháng',100,'9 tháng',43,300000),(44,'Coursera Pro - 9 tháng',100,'9 tháng',44,300000),(45,'Spotify Premium - 9 tháng',100,'9 tháng',45,300000),(46,'Netflix Gift Card - 9 tháng',100,'9 tháng',46,300000),(47,'Steam Wallet 100k - 9 tháng',100,'9 tháng',47,300000),(48,'Disney+ 1 năm - 9 tháng',100,'9 tháng',48,300000),(49,'Slack Pro - 9 tháng',100,'9 tháng',49,300000),(50,'Zoom Business - 9 tháng',100,'9 tháng',50,300000),(51,'Notion Plus - 9 tháng',100,'9 tháng',51,300000),(52,'Trello Premium - 9 tháng',100,'9 tháng',52,300000),(53,'Windows 10 Home Key - 9 tháng',100,'9 tháng',53,300000),(54,'Windows 10 Pro Key - 9 tháng',100,'9 tháng',54,300000),(55,'Windows 11 Home Key - 9 tháng',100,'9 tháng',55,300000),(56,'Windows 11 Pro Key - 9 tháng',100,'9 tháng',56,300000),(57,'NordVPN 1 năm - 9 tháng',100,'9 tháng',57,300000),(58,'ExpressVPN 6 tháng - 9 tháng',100,'9 tháng',58,300000),(59,'Surfshark VPN - 9 tháng',100,'9 tháng',59,300000),(60,'CyberGhost VPN - 9 tháng',100,'9 tháng',60,300000),(61,'Microsoft Office 365 - 1 năm',100,'1 năm',41,500000),(62,'Grammarly Premium - 1 năm',100,'1 năm',42,500000),(63,'Khan Academy Plus - 1 năm',100,'1 năm',43,500000),(64,'Coursera Pro - 1 năm',100,'1 năm',44,500000),(65,'Spotify Premium - 1 năm',100,'1 năm',45,500000),(66,'Netflix Gift Card - 1 năm',100,'1 năm',46,500000),(67,'Steam Wallet 100k - 1 năm',100,'1 năm',47,500000),(68,'Disney+ 1 năm - 1 năm',100,'1 năm',48,500000),(69,'Slack Pro - 1 năm',100,'1 năm',49,500000),(70,'Zoom Business - 1 năm',100,'1 năm',50,500000),(71,'Notion Plus - 1 năm',100,'1 năm',51,500000),(72,'Trello Premium - 1 năm',100,'1 năm',52,500000),(73,'Windows 10 Home Key - 1 năm',100,'1 năm',53,500000),(74,'Windows 10 Pro Key - 1 năm',100,'1 năm',54,500000),(75,'Windows 11 Home Key - 1 năm',100,'1 năm',55,500000),(76,'Windows 11 Pro Key - 1 năm',100,'1 năm',56,500000),(77,'NordVPN 1 năm - 1 năm',100,'1 năm',57,500000),(78,'ExpressVPN 6 tháng - 1 năm',100,'1 năm',58,500000),(79,'Surfshark VPN - 1 năm',100,'1 năm',59,500000),(80,'CyberGhost VPN - 1 năm',100,'1 năm',60,500000),(128,'Microsoft Office 365 - 6 tháng',100,'6 tháng',41,200000),(129,'Grammarly Premium - 6 tháng',100,'6 tháng',42,200000),(130,'Khan Academy Plus - 6 tháng',100,'6 tháng',43,200000),(131,'Coursera Pro - 6 tháng',100,'6 tháng',44,200000),(132,'Spotify Premium - 6 tháng',100,'6 tháng',45,200000),(133,'Netflix Gift Card - 6 tháng',100,'6 tháng',46,200000),(134,'Steam Wallet 100k - 6 tháng',100,'6 tháng',47,200000),(135,'Disney+ 1 năm - 6 tháng',100,'6 tháng',48,200000),(136,'Slack Pro - 6 tháng',100,'6 tháng',49,200000),(137,'Zoom Business - 6 tháng',100,'6 tháng',50,200000),(138,'Notion Plus - 6 tháng',100,'6 tháng',51,200000),(139,'Trello Premium - 6 tháng',100,'6 tháng',52,200000),(140,'Windows 10 Home Key - 6 tháng',100,'6 tháng',53,200000),(141,'Windows 10 Pro Key - 6 tháng',100,'6 tháng',54,200000),(142,'Windows 11 Home Key - 6 tháng',100,'6 tháng',55,200000),(143,'Windows 11 Pro Key - 6 tháng',100,'6 tháng',56,200000),(144,'NordVPN 1 năm - 6 tháng',100,'6 tháng',57,200000),(145,'ExpressVPN 6 tháng - 6 tháng',100,'6 tháng',58,200000),(146,'Surfshark VPN - 6 tháng',100,'6 tháng',59,200000),(147,'CyberGhost VPN - 6 tháng',100,'6 tháng',60,200000);
/*!40000 ALTER TABLE `variant` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-09-26 16:20:00

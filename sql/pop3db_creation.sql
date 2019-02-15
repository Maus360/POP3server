-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

-- -----------------------------------------------------
-- Schema pop3db
-- -----------------------------------------------------
DROP SCHEMA IF EXISTS `pop3db` ;

-- -----------------------------------------------------
-- Schema pop3db
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `pop3db` DEFAULT CHARACTER SET utf8 ;
USE `pop3db` ;

-- -----------------------------------------------------
-- Table `pop3db`.`user_credentials`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `pop3db`.`user_credentials` ;

CREATE TABLE IF NOT EXISTS `pop3db`.`user_credentials` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `email` VARCHAR(45) NOT NULL,
  `password` VARCHAR(45) NOT NULL,
  `locked` INT NOT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;

CREATE UNIQUE INDEX `id_UNIQUE` ON `pop3db`.`user_credentials` (`id` ASC);

CREATE UNIQUE INDEX `email_UNIQUE` ON `pop3db`.`user_credentials` (`email` ASC);


-- -----------------------------------------------------
-- Table `pop3db`.`mail`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `pop3db`.`mail` ;

CREATE TABLE IF NOT EXISTS `pop3db`.`mail` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `content` VARCHAR(500) NULL,
  `markedForDeletion` INT NULL DEFAULT 0,
  `user_credentials_id` INT NOT NULL,
  `UIDL` VARCHAR(500) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_mail_user_credentials`
    FOREIGN KEY (`user_credentials_id`)
    REFERENCES `pop3db`.`user_credentials` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE UNIQUE INDEX `id_UNIQUE` ON `pop3db`.`mail` (`id` ASC);

CREATE INDEX `fk_mail_user_credentials_idx` ON `pop3db`.`mail` (`user_credentials_id` ASC);


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- -----------------------------------------------------
-- Data for table `pop3db`.`user_credentials`
-- -----------------------------------------------------
START TRANSACTION;
USE `pop3db`;
INSERT INTO `pop3db`.`user_credentials` (`id`, `email`, `password`, `locked`) VALUES (1, 'admin@mail.com', 'qwerty123', 0);
INSERT INTO `pop3db`.`user_credentials` (`id`, `email`, `password`, `locked`) VALUES (2, 'bob@mail.com', 'hellobob', 0);
INSERT INTO `pop3db`.`user_credentials` (`id`, `email`, `password`, `locked`) VALUES (3, 'someone@mail.com', 'hellosomeone', 0);

COMMIT;


-- -----------------------------------------------------
-- Data for table `pop3db`.`mail`
-- -----------------------------------------------------
START TRANSACTION;
USE `pop3db`;
INSERT INTO `pop3db`.`mail` (`id`, `content`, `markedForDeletion`, `user_credentials_id`, `UIDL`) VALUES (1, 'Test Message\n\ntest mail from some human', 0, 1, '267c6c43');
INSERT INTO `pop3db`.`mail` (`id`, `content`, `markedForDeletion`, `user_credentials_id`, `UIDL`) VALUES (2, 'Another Test for Bob\n\nanother test mail which is longer \nthen the other one and it is from another human', 0, 2, 'b98727df');
INSERT INTO `pop3db`.`mail` (`id`, `content`, `markedForDeletion`, `user_credentials_id`, `UIDL`) VALUES (3, 'The Truth\n\nlena and max are making \na \npure masterpiece', 0, 2, 'deb3f2b1');

COMMIT;


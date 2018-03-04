create table CUSTOMER
(
	CUSTOMER_ID NUMBER not null
		primary key,
	FIRST_NAME VARCHAR2(20)
)
/

create table BACCOUNT
(
	ACCOUNT_ID NUMBER not null
		primary key,
	CUSTOMER_ID NUMBER
		references CUSTOMER,
	BALANCE NUMBER(10,2),
	B_LOCKED NUMBER(1) not null,
	DELETED NUMBER(1) not null
)
/

create table TRANSFER
(
	ACCOUNT_ID_FROM NUMBER not null
		references BACCOUNT,
	ACCOUNT_ID_TO NUMBER not null
		references BACCOUNT,
	TRANSFER_DATE DATE not null,
	AMOUNT NUMBER(10,2),
	primary key (ACCOUNT_ID_FROM, ACCOUNT_ID_TO, TRANSFER_DATE)
)
/

create PACKAGE OUTER_LAYER AS 

    FUNCTION create_account(customer_id INT) RETURN INTEGER;
    FUNCTION delete_account(acc_id INT) RETURN INTEGER;
    FUNCTION get_balance(acc_id INT) RETURN NUMBER;
    FUNCTION do_transfer(account_id_from INT, account_id_to INT, amount INT) RETURN INTEGER;
    TYPE refcursortype IS REF CURSOR;
    FUNCTION get_transfer_history(acc_id_from INT) RETURN refcursortype;
    
END OUTER_LAYER;
/

create PACKAGE INNER_LAYER AS 

    FUNCTION check_account_id(account__id INT) RETURN BOOLEAN;
    FUNCTION check_customer_id(customer__id INT) RETURN BOOLEAN;
    FUNCTION change_balance(acc_id INT, amount INT) RETURN BOOLEAN;
    FUNCTION insert_transfer_history(account_id_from INT, account_id_to INT, amount INT) RETURN BOOLEAN;
    FUNCTION is_deleted(account__id INT) RETURN BOOLEAN;
    
END INNER_LAYER;
/

create PACKAGE BODY INNER_LAYER AS

  FUNCTION check_account_id(account__id INT) RETURN BOOLEAN AS
    v_success INT;
  BEGIN
    SELECT 1 INTO v_success FROM baccount WHERE baccount.account_id=account__id;
    RETURN TRUE;
  EXCEPTION
    WHEN no_data_found THEN
        RETURN FALSE;
  END check_account_id;
--------------------------------------------------------
  FUNCTION check_customer_id(customer__id INT) RETURN BOOLEAN AS
    v_success INT;
  BEGIN
    SELECT 1 INTO v_success FROM customer WHERE customer.customer_id=customer__id;
    RETURN TRUE;
  EXCEPTION
    WHEN no_data_found THEN
        RETURN FALSE;
  END check_customer_id;
--------------------------------------------------------
  FUNCTION change_balance(acc_id INT, amount INT) RETURN BOOLEAN AS
  BEGIN
    UPDATE baccount SET balance=balance+amount WHERE account_id=acc_id;
    RETURN TRUE;
  END change_balance;
  
  FUNCTION insert_transfer_history(account_id_from INT, account_id_to INT, amount INT) RETURN BOOLEAN AS
  BEGIN
    INSERT INTO transfer (account_id_from, account_id_to, transfer_date, amount) VALUES (account_id_from, account_id_to, SYSDATE(), amount);
    RETURN TRUE;
  END insert_transfer_history;
--------------------------------------------------------
  FUNCTION is_deleted(account__id INT) RETURN BOOLEAN AS
    v_deleted INT;
  BEGIN
    SELECT deleted INTO v_deleted FROM baccount WHERE account_id=account__id;
    -- 1 means deleted, 0 or everything else not deleted
    RETURN v_deleted = 1;
  END is_deleted;

END INNER_LAYER;
/

create PACKAGE BODY OUTER_LAYER AS

  FUNCTION create_account(customer_id INT) RETURN INTEGER AS
    account_id INT;
    v_cust_valid BOOLEAN;
  BEGIN
    v_cust_valid := INNER_LAYER.CHECK_CUSTOMER_ID(customer_id);
    IF NOT v_cust_valid THEN
        RETURN -1;
    END IF;
    account_id := baccount_id.NEXTVAL;
    INSERT INTO baccount VALUES (account_id, customer_id, 0, 0, 0);
    RETURN account_id;
  END create_account;
--------------------------------------------------------
  FUNCTION delete_account(acc_id INT) RETURN INTEGER AS
    v_acc_valid BOOLEAN;
    v_deleted BOOLEAN;
  BEGIN
    v_acc_valid := INNER_LAYER.CHECK_ACCOUNT_ID(acc_id);
    IF NOT v_acc_valid THEN
        RETURN -1;
    END IF;
    v_deleted := INNER_LAYER.IS_DELETED(acc_id);
    IF v_deleted THEN
        RETURN -2;
    END IF;
    UPDATE baccount SET deleted=1 WHERE account_id=acc_id;
    RETURN 0;
  END delete_account;
--------------------------------------------------------
  FUNCTION get_balance(acc_id INT) RETURN NUMBER AS
    v_balance baccount.balance%type;
  BEGIN
    SELECT balance INTO v_balance FROM baccount WHERE account_id = acc_id;
    RETURN v_balance;
  END get_balance;
--------------------------------------------------------
  FUNCTION do_transfer(account_id_from INT, account_id_to INT, amount INT) RETURN INTEGER AS
    v_balance baccount.balance%type;
    v_success BOOLEAN;
  BEGIN
    v_balance := get_balance(account_id_from);
    IF v_balance - amount < -100 THEN
        RETURN -1;
    END IF;
    
    v_success := INNER_LAYER.change_balance(account_id_from, -amount);
    v_success := INNER_LAYER.change_balance(account_id_to, +amount);
    
    v_success := INNER_LAYER.insert_transfer_history(account_id_from, account_id_to, amount);
    
    RETURN 0;
  END do_transfer;
--------------------------------------------------------
  FUNCTION get_transfer_history(acc_id_from INT) RETURN refcursortype AS
    cur_transhist refcursortype;
  BEGIN
    OPEN cur_transhist FOR
        SELECT account_id_from, account_id_to, transfer_date, amount FROM transfer WHERE account_id_from=acc_id_from;
        RETURN cur_transhist;
  END get_transfer_history;

END OUTER_LAYER;
/

create sequence BACCOUNT_ID
	minvalue 7
/


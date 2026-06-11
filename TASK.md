# Mini Banking Application

## Overview

Membuat aplikasi mobile banking sederhana menggunakan Spring Boot, JPA, dan PostgreSQL.

Aplikasi terdiri dari fitur Customer, Account, dan Transaction.

---

# Database Design

## Master Table

### mst_customer

| Column      | Type        | Description    |
| ----------- | ----------- | -------------- |
| customer_id | UUID/String | Primary Key    |
| name        | String      | Nama customer  |
| email       | String      | Email customer |
| phone       | String      | Nomor telepon  |
| address     | String      | Kota/Alamat    |

---

### mst_account

| Column         | Type       | Description                         |
| -------------- | ---------- | ----------------------------------- |
| account_id     | String     | Primary Key (8 digit random unique) |
| account_number | String     | Nomor rekening unique               |
| balance        | BigDecimal | Saldo rekening                      |
| customer_id    | String     | FK ke mst_customer                  |

Relationship:

* 1 Customer dapat memiliki banyak Account
* 1 Account hanya dimiliki oleh 1 Customer

---

# Transaction Table

### trx_transaction

| Column                 | Type        | Description                      |
| ---------------------- | ----------- | -------------------------------- |
| transaction_id         | UUID/String | Primary Key                      |
| transaction_type       | Enum        | TOPUP, TRANSFER_IN, TRANSFER_OUT |
| amount                 | BigDecimal  | Nominal transaksi                |
| transaction_date       | Timestamp   | Waktu transaksi                  |
| source_account_id      | String      | Rekening pengirim                |
| destination_account_id | String      | Rekening penerima                |
| balance_before         | BigDecimal  | Saldo sebelum transaksi          |
| balance_after          | BigDecimal  | Saldo setelah transaksi          |

---

# Configuration

Tambahkan biaya admin pada file:

application.yml

yaml
bank:
transfer:
admin-fee: 2500


Biaya admin harus diambil dari configuration, bukan hardcoded.

---

# Features

## Customer

### Register Customer

Membuat customer baru.

### View Customer

* Get customer by id
* Get all customer

### Update Customer

Mengubah data customer.

### Delete Customer

Menghapus customer.

---

## Account

### Create Account

Membuat rekening baru untuk customer.

Rules:

* Customer harus ada.
* Account number harus unique.
* Account id berupa 8 digit angka random unique.

### View Account

* Get account by id
* Get account by account number
* Get all account

---

## Transaction

Semua transaksi diletakkan dalam:

* TransactionController
* TransactionService

---

### Top Up

Menambahkan saldo ke rekening.

Rules:

* Account harus ada.
* Nominal top up minimal Rp10.000.
* Saldo rekening harus bertambah sesuai nominal top up.

History transaksi harus tersimpan ke trx_transaction.

---

### Transfer

Transfer saldo antar rekening.

Rules:

* Source account harus ada.
* Destination account harus ada.
* Tidak boleh transfer ke rekening sendiri.
* Nominal transfer minimal Rp10.000.
* Saldo pengirim harus mencukupi.
* Biaya admin diambil dari configuration.
* Saldo pengirim harus dikurangi.
* Saldo penerima harus ditambah.

Setiap transfer menghasilkan:

#### History Pengirim

transaction_type:

text
TRANSFER_OUT


#### History Penerima

transaction_type:

text
TRANSFER_IN


Dengan demikian satu transfer menghasilkan dua history transaksi.

---

# Transaction History

## Get All Transaction

Menampilkan seluruh transaksi.

Requirement:

* Menggunakan pagination.
* Menggunakan sorting berdasarkan tanggal transaksi.

---

## Get Transaction By Account

Menampilkan seluruh history transaksi milik account tertentu.

Requirement:

* Pagination.
* Sorting.

---

## Filter Transaction

Mendukung filter:

* ALL
* TOPUP
* TRANSFER_IN
* TRANSFER_OUT

Contoh:

text
/account/{id}/transactions?type=TRANSFER_OUT


---

# Validation

Semua validasi harus dibuat secara detail dan konsisten.

Contoh:

### Customer Not Found

text
Customer not found


### Account Not Found

text
Account not found


### Destination Account Not Found

text
Destination account not found


### Insufficient Balance

text
Insufficient balance


### Minimum Transfer

text
Minimum transfer amount is Rp10.000


### Minimum Top Up

text
Minimum top up amount is Rp10.000


### Invalid Transfer

text
Cannot transfer to same account


### Duplicate Account Number

text
Account number already exists


---

# Endpoint Summary

## Customer

POST /customers/register

GET /customers

GET /customers/{id}

PUT /customers/{id}

DELETE /customers/{id}

---

## Account

POST /accounts

GET /accounts

GET /accounts/{id}

GET /accounts/account-number/{accountNumber}

---

## Transaction

POST /transactions/topup

POST /transactions/transfer

GET /transactions

GET /transactions/account/{accountId}

GET /transactions/account/{accountId}?type=TRANSFER_OUT
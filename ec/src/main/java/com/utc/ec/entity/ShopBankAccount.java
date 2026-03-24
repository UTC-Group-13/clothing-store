package com.utc.ec.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "shop_bank_account")
public class ShopBankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Mã ngân hàng theo chuẩn VietQR (VD: VCB, TCB, MB, ACB...) */
    @Column(name = "bank_id", nullable = false, length = 20)
    private String bankId;

    /** Tên ngân hàng đầy đủ */
    @Column(name = "bank_name", nullable = false, length = 200)
    private String bankName;

    /** Số tài khoản ngân hàng */
    @Column(name = "account_number", nullable = false, length = 50)
    private String accountNumber;

    /** Tên chủ tài khoản */
    @Column(name = "account_holder_name", nullable = false, length = 200)
    private String accountHolderName;

    /** Logo ngân hàng (URL) */
    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    /** Trạng thái hoạt động (chỉ 1 tài khoản active tại 1 thời điểm) */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}


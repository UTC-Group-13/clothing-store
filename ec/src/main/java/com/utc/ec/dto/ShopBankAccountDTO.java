package com.utc.ec.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Thông tin tài khoản ngân hàng của shop")
public class ShopBankAccountDTO {

    @Schema(description = "ID", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer id;

    @NotBlank(message = "Mã ngân hàng không được để trống")
    @Size(max = 20)
    @Schema(description = "Mã ngân hàng VietQR (VCB, TCB, MB, ACB...)", example = "MB")
    private String bankId;

    @NotBlank(message = "Tên ngân hàng không được để trống")
    @Size(max = 200)
    @Schema(description = "Tên ngân hàng", example = "Ngân hàng Quân Đội (MB Bank)")
    private String bankName;

    @NotBlank(message = "Số tài khoản không được để trống")
    @Size(max = 50)
    @Schema(description = "Số tài khoản", example = "0123456789")
    private String accountNumber;

    @NotBlank(message = "Tên chủ tài khoản không được để trống")
    @Size(max = 200)
    @Schema(description = "Tên chủ tài khoản (viết HOA, không dấu)", example = "NGUYEN VAN A")
    private String accountHolderName;

    @Schema(description = "Logo ngân hàng (URL)")
    private String logoUrl;

    @Schema(description = "Trạng thái hoạt động")
    private Boolean isActive;
}


package org.example.error.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ErrorResponse {

  /** ステータスコード */
  private Integer status;
  /** エラーコード */
  private String code;
  /** 概要 */
  private String summary;
  /** 開発者向けメッセージ */
  private String detail;
  /** ユーザ向けメッセージ */
  private String message;
}

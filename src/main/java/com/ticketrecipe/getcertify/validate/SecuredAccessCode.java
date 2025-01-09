package com.ticketrecipe.getcertify.validate;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SecuredAccessCode {
    private String value;
}
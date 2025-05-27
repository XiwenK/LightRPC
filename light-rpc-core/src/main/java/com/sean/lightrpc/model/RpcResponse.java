package com.sean.lightrpc.model;

import com.sean.lightrpc.constant.RpcConstant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RpcResponse implements Serializable {

    private Object data;

    /**
     * response data type
     */
    private Class<?> dataType;

    private String message;

    private Exception exception;

}

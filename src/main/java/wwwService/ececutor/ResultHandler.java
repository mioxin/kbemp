package com.gmail.mrmioxin.kbemp.wwwService.ececutor;


import org.apache.http.HttpEntity;

public interface ResultHandler<T> {
	T handle(HttpEntity result);
}

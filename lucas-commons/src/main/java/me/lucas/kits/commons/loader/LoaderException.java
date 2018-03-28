/*
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.lucas.kits.commons.loader;

/**
 * 加载异常处理类.
 * @author yanghe
 */
public class LoaderException extends RuntimeException {

    private static final long serialVersionUID = 4559314971197577659L;

    /**
     * 
     * @param message the message
     */
    public LoaderException(final String message) {
        super(message);
    }

    /**
     * 
     * @param message the message
     * @param cause the cause
     */
    public LoaderException(final String message, final Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getMessage() {
        return "加载异常: " + super.getMessage();
    }
}

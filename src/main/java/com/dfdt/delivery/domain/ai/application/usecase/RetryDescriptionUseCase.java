package com.dfdt.delivery.domain.ai.application.usecase;

import com.dfdt.delivery.domain.ai.application.dto.RetryDescriptionCommand;
import com.dfdt.delivery.domain.ai.application.dto.RetryDescriptionResult;

public interface RetryDescriptionUseCase {
    RetryDescriptionResult execute(RetryDescriptionCommand command);
}

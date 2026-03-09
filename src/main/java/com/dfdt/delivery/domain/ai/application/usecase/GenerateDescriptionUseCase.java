package com.dfdt.delivery.domain.ai.application.usecase;

import com.dfdt.delivery.domain.ai.application.dto.GenerateDescriptionCommand;
import com.dfdt.delivery.domain.ai.application.dto.GenerateDescriptionResult;

public interface GenerateDescriptionUseCase {

    GenerateDescriptionResult execute(GenerateDescriptionCommand command);
}

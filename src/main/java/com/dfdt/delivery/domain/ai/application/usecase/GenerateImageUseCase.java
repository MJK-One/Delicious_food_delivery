package com.dfdt.delivery.domain.ai.application.usecase;

import com.dfdt.delivery.domain.ai.application.dto.GenerateImageCommand;
import com.dfdt.delivery.domain.ai.application.dto.GenerateImageResult;

public interface GenerateImageUseCase {
    GenerateImageResult execute(GenerateImageCommand command);
}
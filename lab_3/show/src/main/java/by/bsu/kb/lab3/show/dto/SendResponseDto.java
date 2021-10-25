package by.bsu.kb.lab3.show.dto;

import by.bsu.kb.schepov.lab3.utils.Signature;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SendResponseDto {
    private String gotMessage;
    private Signature gotSignature;
    private String result;
}

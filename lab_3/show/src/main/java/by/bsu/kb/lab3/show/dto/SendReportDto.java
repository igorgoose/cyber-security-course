package by.bsu.kb.lab3.show.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SendReportDto {
    private String initialMessage;
    private SendResponseDto responseFromUserB;
}

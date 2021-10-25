package by.bsu.kb.lab3.show.dto;

import by.bsu.kb.schepov.lab3.utils.BasePoint;
import by.bsu.kb.schepov.lab3.utils.Point;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ExchangeRequestDto {
    private BasePoint basePoint;
    private Point publicKey;
}

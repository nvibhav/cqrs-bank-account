package io.nikhil.cqrsbankaccount.core.messages

import lombok.AllArgsConstructor
import lombok.Data
import lombok.NoArgsConstructor
import lombok.experimental.SuperBuilder

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
abstract class Message(
    var id: String? = null,
)

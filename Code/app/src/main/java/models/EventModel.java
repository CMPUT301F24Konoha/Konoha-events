package models;

import androidx.annotation.Nullable;

import interfaces.HasImage;
import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@Getter
@AllArgsConstructor
@Builder
public class EventModel implements HasImage {
    @NonNull
    private String id;
    @Nullable
    private String imageUrl;
    //... and other properties
}

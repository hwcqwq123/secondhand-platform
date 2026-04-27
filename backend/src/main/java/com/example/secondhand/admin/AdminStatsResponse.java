package com.example.secondhand.admin;

public record AdminStatsResponse(
        Long userTotal,
        Long userBanned,
        Long itemTotal,
        Long itemAvailable,
        Long itemReserved,
        Long itemSold,
        Long itemOffShelf,
        Long itemDeleted,
        Long orderTotal,
        Long orderCreated,
        Long orderPaid,
        Long orderCanceled
) {}
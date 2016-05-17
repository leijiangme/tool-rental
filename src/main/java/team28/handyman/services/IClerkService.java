package team28.handyman.services;

import team28.handyman.domain.Clerk;

public interface IClerkService {
	Clerk byId(final String id, String password);
}

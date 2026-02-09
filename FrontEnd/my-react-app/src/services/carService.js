import api from "./api";

export const getAllCars = () => {
  // API Gateway route for car service is configured as /api/cars/**
  return api.get("/api/cars");
};

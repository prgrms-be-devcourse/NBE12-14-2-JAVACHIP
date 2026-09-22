export type ApiResponse<T> = {
  resultCode: number;
  message: string;
  data: T;
};

export class ApiError extends Error {
  constructor(
    message: string,
    public readonly status: number,
  ) {
    super(message);
    this.name = "ApiError";
  }
}

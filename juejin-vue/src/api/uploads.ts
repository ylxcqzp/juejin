import request from './request'

/**
 * 上传图片文件，返回公开访问 URL
 */
export function uploadImage(file: File): Promise<string> {
  const formData = new FormData()
  formData.append('file', file)
  // Axios 自动检测 FormData 并设置 multipart/form-data + boundary
  return request.post('/uploads/images', formData).then(r => r.data.data as string)
}

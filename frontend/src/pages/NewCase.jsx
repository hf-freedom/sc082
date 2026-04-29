import React, { useState, useEffect } from 'react'
import {
  Form,
  Input,
  Select,
  Button,
  Card,
  message,
  Divider,
  Space,
  Alert,
  Row,
  Col,
  Tag,
  Checkbox,
} from 'antd'
import { ArrowLeftOutlined, SaveOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { caseApi, itemApi, materialApi } from '../api'

const { Option } = Select
const { TextArea } = Input

function NewCase() {
  const navigate = useNavigate()
  const [form] = Form.useForm()
  const [items, setItems] = useState([])
  const [selectedItem, setSelectedItem] = useState(null)
  const [requiredMaterials, setRequiredMaterials] = useState([])
  const [optionalMaterials, setOptionalMaterials] = useState([])
  const [loading, setLoading] = useState(false)
  const [submittedMaterials, setSubmittedMaterials] = useState([])

  useEffect(() => {
    loadItems()
  }, [])

  const loadItems = async () => {
    try {
      const data = await itemApi.listActive()
      setItems(data || [])
    } catch (error) {
      console.error('加载事项列表失败:', error)
    }
  }

  const handleItemChange = async (itemId) => {
    setSelectedItem(itemId)
    setSubmittedMaterials([])
    form.setFieldValue('submittedMaterials', [])

    try {
      const [required, optional] = await Promise.all([
        caseApi.getRequiredMaterials(itemId),
        caseApi.getOptionalMaterials(itemId),
      ])
      setRequiredMaterials(required || [])
      setOptionalMaterials(optional || [])
    } catch (error) {
      console.error('加载材料列表失败:', error)
    }
  }

  const handleMaterialChange = (materialId, checked) => {
    if (checked) {
      const allMaterials = [...requiredMaterials, ...optionalMaterials]
      const material = allMaterials.find((m) => m.id === materialId)
      if (material) {
        setSubmittedMaterials((prev) => [
          ...prev,
          {
            materialId: material.id,
            materialName: material.name,
            fileId: `file_${Date.now()}_${materialId}`,
            fileName: `${material.name}.pdf`,
            fileType: 'pdf',
            fileSize: 1024 * 1024,
            valid: true,
          },
        ])
      }
    } else {
      setSubmittedMaterials((prev) => prev.filter((m) => m.materialId !== materialId))
    }
  }

  const onFinish = async (values) => {
    setLoading(true)
    try {
      const caseData = {
        itemId: values.itemId,
        applicantName: values.applicantName,
        applicantIdCard: values.applicantIdCard,
        applicantPhone: values.applicantPhone,
        applicantAddress: values.applicantAddress,
        submittedMaterials: submittedMaterials,
        remark: values.remark,
      }

      const result = await caseApi.submit(caseData)
      message.success('办件提交成功！案件编号: ' + result.caseNumber)
      navigate(`/cases/${result.id}`)
    } catch (error) {
      message.error('提交失败: ' + (error.message || '未知错误'))
    } finally {
      setLoading(false)
    }
  }

  const selectedItemInfo = items.find((i) => i.id === selectedItem)

  return (
    <div>
      <Row justify="space-between" style={{ marginBottom: 24 }}>
        <Col>
          <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/cases')}>
            返回列表
          </Button>
        </Col>
        <Col>
          <h2 style={{ margin: 0 }}>新建办件</h2>
        </Col>
        <Col></Col>
      </Row>

      <Form
        form={form}
        layout="vertical"
        onFinish={onFinish}
        initialValues={{}}
      >
        <Card title="基本信息" style={{ marginBottom: 16 }}>
          <Row gutter={24}>
            <Col span={12}>
              <Form.Item
                name="itemId"
                label="办理事项"
                rules={[{ required: true, message: '请选择办理事项' }]}
              >
                <Select placeholder="请选择办理事项" onChange={handleItemChange}>
                  {items.map((item) => (
                    <Option key={item.id} value={item.id}>
                      {item.name} ({item.category})
                    </Option>
                  ))}
                </Select>
              </Form.Item>
            </Col>
            {selectedItemInfo && (
              <Col span={12}>
                <Form.Item label="办理时限">
                  <Input
                    disabled
                    value={`${selectedItemInfo.processingDays} 个工作日 (提前 ${selectedItemInfo.warningDays} 天预警)`}
                  />
                </Form.Item>
              </Col>
            )}
          </Row>

          {selectedItemInfo && (
            <Alert
              message="事项说明"
              description={selectedItemInfo.description}
              type="info"
              showIcon
              style={{ marginBottom: 16 }}
            />
          )}
        </Card>

        <Card title="申请人信息" style={{ marginBottom: 16 }}>
          <Row gutter={24}>
            <Col span={8}>
              <Form.Item
                name="applicantName"
                label="申请人姓名"
                rules={[{ required: true, message: '请输入申请人姓名' }]}
              >
                <Input placeholder="请输入申请人姓名" />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item
                name="applicantIdCard"
                label="身份证号"
                rules={[
                  { required: true, message: '请输入身份证号' },
                  { pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/, message: '身份证号格式不正确' },
                ]}
              >
                <Input placeholder="请输入身份证号" maxLength={18} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item
                name="applicantPhone"
                label="联系电话"
                rules={[
                  { required: true, message: '请输入联系电话' },
                  { pattern: /^1[3-9]\d{9}$/, message: '手机号码格式不正确' },
                ]}
              >
                <Input placeholder="请输入联系电话" maxLength={11} />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={24}>
            <Col span={24}>
              <Form.Item
                name="applicantAddress"
                label="联系地址"
              >
                <Input placeholder="请输入联系地址" />
              </Form.Item>
            </Col>
          </Row>
        </Card>

        {selectedItem && (
          <Card title="材料清单" style={{ marginBottom: 16 }}>
            {requiredMaterials.length > 0 && (
              <>
                <Divider orientation="left">必填材料</Divider>
                <Row gutter={[16, 16]}>
                  {requiredMaterials.map((material) => {
                    const isSubmitted = submittedMaterials.some(
                      (m) => m.materialId === material.id
                    )
                    return (
                      <Col span={8} key={material.id}>
                        <Card
                          size="small"
                          style={{
                            borderColor: isSubmitted ? '#52c41a' : '#d9d9d9',
                            backgroundColor: isSubmitted ? '#f6ffed' : '#fff',
                          }}
                        >
                          <Space direction="vertical" style={{ width: '100%' }}>
                            <Space>
                              <Checkbox
                                checked={isSubmitted}
                                onChange={(e) => handleMaterialChange(material.id, e.target.checked)}
                              >
                                <span style={{ fontWeight: isSubmitted ? 'bold' : 'normal' }}>
                                  {material.name}
                                  <span style={{ color: '#ff4d4f', marginLeft: 4 }}>*</span>
                                </span>
                              </Checkbox>
                            </Space>
                            <span style={{ fontSize: 12, color: '#666' }}>
                              {material.description}
                            </span>
                            {isSubmitted && (
                              <Tag color="success">已提交</Tag>
                            )}
                          </Space>
                        </Card>
                      </Col>
                    )
                  })}
                </Row>
              </>
            )}

            {optionalMaterials.length > 0 && (
              <>
                <Divider orientation="left">可选材料</Divider>
                <Row gutter={[16, 16]}>
                  {optionalMaterials.map((material) => {
                    const isSubmitted = submittedMaterials.some(
                      (m) => m.materialId === material.id
                    )
                    return (
                      <Col span={8} key={material.id}>
                        <Card
                          size="small"
                          style={{
                            borderColor: isSubmitted ? '#1890ff' : '#d9d9d9',
                            backgroundColor: isSubmitted ? '#e6f7ff' : '#fff',
                          }}
                        >
                          <Space direction="vertical" style={{ width: '100%' }}>
                            <Space>
                              <Checkbox
                                checked={isSubmitted}
                                onChange={(e) => handleMaterialChange(material.id, e.target.checked)}
                              >
                                <span style={{ fontWeight: isSubmitted ? 'bold' : 'normal' }}>
                                  {material.name}
                                </span>
                              </Checkbox>
                            </Space>
                            <span style={{ fontSize: 12, color: '#666' }}>
                              {material.description}
                            </span>
                            {isSubmitted && (
                              <Tag color="processing">已提交</Tag>
                            )}
                          </Space>
                        </Card>
                      </Col>
                    )
                  })}
                </Row>
              </>
            )}

            {selectedItem && requiredMaterials.length > 0 && (
              <Alert
                message={`已提交 ${submittedMaterials.filter((m) =>
                  requiredMaterials.some((rm) => rm.id === m.materialId)
                ).length} / ${requiredMaterials.length} 项必填材料`}
                type={
                  submittedMaterials.filter((m) =>
                    requiredMaterials.some((rm) => rm.id === m.materialId)
                  ).length >= requiredMaterials.length
                    ? 'success'
                    : 'warning'
                }
                showIcon
                style={{ marginTop: 16 }}
              />
            )}
          </Card>
        )}

        <Card title="备注" style={{ marginBottom: 16 }}>
          <Form.Item name="remark">
            <TextArea rows={4} placeholder="请输入备注信息（可选）" />
          </Form.Item>
        </Card>

        <Form.Item>
          <Space>
            <Button type="primary" htmlType="submit" loading={loading} icon={<SaveOutlined />}>
              提交办件
            </Button>
            <Button onClick={() => form.resetFields()}>重置</Button>
            <Button onClick={() => navigate('/cases')}>取消</Button>
          </Space>
        </Form.Item>
      </Form>
    </div>
  )
}

export default NewCase

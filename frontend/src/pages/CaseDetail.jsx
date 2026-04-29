import React, { useState, useEffect } from 'react'
import {
  Card,
  Descriptions,
  Tag,
  Button,
  Space,
  Modal,
  Form,
  Select,
  Input,
  message,
  Divider,
  Table,
  List,
  Alert,
  Timeline,
  Row,
  Col,
  Statistic,
} from 'antd'
import {
  ArrowLeftOutlined,
  CheckOutlined,
  CloseOutlined,
  UndoOutlined,
  RollbackOutlined,
  FileTextOutlined,
  ClockCircleOutlined,
  SyncOutlined,
  WarningOutlined,
} from '@ant-design/icons'
import { useNavigate, useParams } from 'react-router-dom'
import { caseApi, supervisionApi, documentApi } from '../api'
import dayjs from 'dayjs'

const { Option } = Select
const { TextArea } = Input

const statusMap = {
  DRAFT: { text: '草稿', color: 'default' },
  SUBMITTED: { text: '已提交', color: 'blue' },
  PENDING_ACCEPT: { text: '待受理', color: 'orange' },
  ACCEPTED: { text: '已受理', color: 'cyan' },
  PENDING_SUPPLEMENT: { text: '待补正', color: 'gold' },
  IN_APPROVAL: { text: '审批中', color: 'purple' },
  APPROVED: { text: '审批通过', color: 'green' },
  REJECTED: { text: '审批驳回', color: 'red' },
  COMPLETED: { text: '已办结', color: 'success' },
  WITHDRAWN: { text: '已撤回', color: 'default' },
  OVERDUE: { text: '已超期', color: 'error' },
}

const approvalResultMap = {
  PASS: { text: '通过', color: 'success' },
  REJECT: { text: '驳回', color: 'error' },
  RETURN_TO_SUPPLEMENT: { text: '退回补正', color: 'warning' },
  TRANSFER: { text: '转交', color: 'processing' },
}

function CaseDetail() {
  const navigate = useNavigate()
  const { id } = useParams()
  const [caseData, setCaseData] = useState(null)
  const [loading, setLoading] = useState(false)
  const [supervisionRecords, setSupervisionRecords] = useState([])

  const [acceptModalVisible, setAcceptModalVisible] = useState(false)
  const [supplementModalVisible, setSupplementModalVisible] = useState(false)
  const [approveModalVisible, setApproveModalVisible] = useState(false)
  const [withdrawModalVisible, setWithdrawModalVisible] = useState(false)
  const [documentModalVisible, setDocumentModalVisible] = useState(false)
  const [forceOverdueConfirmVisible, setForceOverdueConfirmVisible] = useState(false)

  const [form] = Form.useForm()
  const [documentForm] = Form.useForm()

  const loadCaseData = async () => {
    setLoading(true)
    try {
      const data = await caseApi.getById(id)
      setCaseData(data)
    } catch (error) {
      message.error('加载办件详情失败: ' + (error.message || '未知错误'))
    } finally {
      setLoading(false)
    }
  }

  const loadSupervisionRecords = async () => {
    try {
      const data = await supervisionApi.getRecordsByCaseId(id)
      setSupervisionRecords(data || [])
    } catch (error) {
      console.error('加载督办记录失败:', error)
    }
  }

  useEffect(() => {
    if (id) {
      loadCaseData()
      loadSupervisionRecords()
    }
  }, [id])

  const handleAccept = async (values) => {
    try {
      await caseApi.accept(id, values.handler)
      message.success('受理成功')
      setAcceptModalVisible(false)
      loadCaseData()
    } catch (error) {
      message.error('受理失败: ' + (error.message || '未知错误'))
    }
  }

  const handleSupplement = async (values) => {
    try {
      const materials = []
      if (caseData?.missingMaterialIds) {
        caseData.missingMaterialIds.forEach((materialId, index) => {
          materials.push({
            materialId: materialId,
            materialName: `补正材料${index + 1}`,
            fileId: `supplement_${Date.now()}_${index}`,
            fileName: `补正材料${index + 1}.pdf`,
            fileType: 'pdf',
            fileSize: 1024 * 1024,
            valid: true,
          })
        })
      }
      await caseApi.supplement(id, materials)
      message.success('材料补正成功')
      setSupplementModalVisible(false)
      loadCaseData()
    } catch (error) {
      message.error('补正失败: ' + (error.message || '未知错误'))
    }
  }

  const handleApprove = async (values) => {
    try {
      await caseApi.approve(id, {
        approver: values.approver,
        result: values.result,
        comment: values.comment,
      })
      message.success('审批成功')
      setApproveModalVisible(false)
      form.resetFields()
      loadCaseData()
    } catch (error) {
      message.error('审批失败: ' + (error.message || '未知错误'))
    }
  }

  const handleWithdraw = async (values) => {
    try {
      await caseApi.withdraw(id, values.reason)
      message.success('撤回成功')
      setWithdrawModalVisible(false)
      loadCaseData()
    } catch (error) {
      message.error('撤回失败: ' + (error.message || '未知错误'))
    }
  }

  const handleGenerateDocument = async (values) => {
    try {
      await documentApi.generate({
        caseId: id,
        documentType: values.documentType,
        content: values.content,
      })
      message.success('生成文档成功')
      setDocumentModalVisible(false)
      documentForm.resetFields()
      loadCaseData()
    } catch (error) {
      message.error('生成文档失败: ' + (error.message || '未知错误'))
    }
  }

  const handleForceOverdue = async () => {
    try {
      await supervisionApi.forceOverdue(id)
      message.success('办件已标记为超期')
      setForceOverdueConfirmVisible(false)
      loadCaseData()
      loadSupervisionRecords()
    } catch (error) {
      message.error('标记超期失败: ' + (error.message || '未知错误'))
    }
  }

  const getStatusInfo = (status) => {
    return statusMap[status] || { text: status, color: 'default' }
  }

  const getRemainingDays = () => {
    if (!caseData?.dueDate) return null
    const due = dayjs(caseData.dueDate)
    const now = dayjs()
    const diff = due.diff(now, 'day')
    return diff
  }

  const remainingDays = getRemainingDays()

  const canAccept = caseData?.status === 'PENDING_ACCEPT'
  const canSupplement = caseData?.status === 'PENDING_SUPPLEMENT'
  const canApprove = caseData?.status === 'IN_APPROVAL'
  const canWithdraw = ['SUBMITTED', 'PENDING_ACCEPT', 'PENDING_SUPPLEMENT', 'ACCEPTED', 'IN_APPROVAL'].includes(
    caseData?.status
  )
  const canGenerateDocument = caseData?.status === 'COMPLETED'
  const canForceOverdue =
    caseData?.dueDate &&
    !['COMPLETED', 'WITHDRAWN', 'REJECTED', 'OVERDUE'].includes(caseData?.status)

  const materialColumns = [
    {
      title: '材料名称',
      dataIndex: 'materialName',
      key: 'materialName',
    },
    {
      title: '文件名称',
      dataIndex: 'fileName',
      key: 'fileName',
    },
    {
      title: '文件类型',
      dataIndex: 'fileType',
      key: 'fileType',
    },
    {
      title: '提交时间',
      dataIndex: 'submittedAt',
      key: 'submittedAt',
      render: (time) => (time ? dayjs(time).format('YYYY-MM-DD HH:mm') : '-'),
    },
    {
      title: '状态',
      dataIndex: 'valid',
      key: 'valid',
      render: (valid) => (
        <Tag color={valid ? 'success' : 'error'}>{valid ? '有效' : '无效'}</Tag>
      ),
    },
  ]

  return (
    <div>
      <Row justify="space-between" style={{ marginBottom: 24 }}>
        <Col>
          <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/cases')}>
            返回列表
          </Button>
        </Col>
        <Col>
          <h2 style={{ margin: 0 }}>办件详情</h2>
        </Col>
        <Col>
          <Space>
            {canAccept && (
              <Button
                type="primary"
                icon={<CheckOutlined />}
                onClick={() => setAcceptModalVisible(true)}
              >
                受理
              </Button>
            )}
            {canSupplement && (
              <Button
                type="primary"
                icon={<SyncOutlined />}
                onClick={() => setSupplementModalVisible(true)}
              >
                补正材料
              </Button>
            )}
            {canApprove && (
              <Button
                type="primary"
                icon={<CheckOutlined />}
                onClick={() => setApproveModalVisible(true)}
              >
                审批
              </Button>
            )}
            {canWithdraw && (
              <Button
                danger
                icon={<UndoOutlined />}
                onClick={() => setWithdrawModalVisible(true)}
              >
                撤回
              </Button>
            )}
            {canGenerateDocument && (
              <Button
                icon={<FileTextOutlined />}
                onClick={() => setDocumentModalVisible(true)}
              >
                生成证照
              </Button>
            )}
            {canForceOverdue && (
              <Button
                danger
                icon={<WarningOutlined />}
                onClick={() => setForceOverdueConfirmVisible(true)}
              >
                手动超期
              </Button>
            )}
            <Button icon={<SyncOutlined />} onClick={loadCaseData}>
              刷新
            </Button>
          </Space>
        </Col>
      </Row>

      {loading ? (
        <Card loading={true} />
      ) : caseData ? (
        <>
          {remainingDays !== null && remainingDays <= 3 && remainingDays > 0 && (
            <Alert
              message={`即将超期预警`}
              description={`该办件距离到期日还有 ${remainingDays} 天，请尽快处理！`}
              type="warning"
              showIcon
              style={{ marginBottom: 16 }}
            />
          )}
          {remainingDays !== null && remainingDays < 0 && (
            <Alert
              message={`办件已超期`}
              description={`该办件已超期 ${Math.abs(remainingDays)} 天！`}
              type="error"
              showIcon
              style={{ marginBottom: 16 }}
            />
          )}

          <Row gutter={16} style={{ marginBottom: 16 }}>
            <Col span={6}>
              <Card>
                <Statistic
                  title="当前状态"
                  valueRender={() => {
                    const info = getStatusInfo(caseData.status)
                    return <Tag color={info.color} style={{ fontSize: 16 }}>{info.text}</Tag>
                  }}
                />
              </Card>
            </Col>
            <Col span={6}>
              <Card>
                <Statistic
                  title="剩余天数"
                  value={remainingDays === null ? '-' : remainingDays}
                  suffix="天"
                  valueStyle={{
                    color: remainingDays === null ? '#999' : remainingDays <= 0 ? '#ff4d4f' : remainingDays <= 3 ? '#fa8c16' : '#52c41a',
                  }}
                  prefix={<ClockCircleOutlined />}
                />
              </Card>
            </Col>
            <Col span={6}>
              <Card>
                <Statistic
                  title="当前节点"
                  value={caseData.currentNodeName || '-'}
                />
              </Card>
            </Col>
            <Col span={6}>
              <Card>
                <Statistic
                  title="经办人"
                  value={caseData.handler || '未分配'}
                />
              </Card>
            </Col>
          </Row>

          <Card title="基本信息" style={{ marginBottom: 16 }}>
            <Descriptions bordered column={2}>
              <Descriptions.Item label="案件编号">{caseData.caseNumber}</Descriptions.Item>
              <Descriptions.Item label="事项名称">{caseData.itemName}</Descriptions.Item>
              <Descriptions.Item label="申请人">{caseData.applicantName}</Descriptions.Item>
              <Descriptions.Item label="身份证号">{caseData.applicantIdCard}</Descriptions.Item>
              <Descriptions.Item label="联系电话">{caseData.applicantPhone}</Descriptions.Item>
              <Descriptions.Item label="联系地址">{caseData.applicantAddress || '-'}</Descriptions.Item>
              <Descriptions.Item label="提交时间">
                {caseData.submittedAt ? dayjs(caseData.submittedAt).format('YYYY-MM-DD HH:mm:ss') : '-'}
              </Descriptions.Item>
              <Descriptions.Item label="受理时间">
                {caseData.acceptedAt ? dayjs(caseData.acceptedAt).format('YYYY-MM-DD HH:mm:ss') : '-'}
              </Descriptions.Item>
              <Descriptions.Item label="到期时间">
                {caseData.dueDate ? dayjs(caseData.dueDate).format('YYYY-MM-DD HH:mm:ss') : '-'}
              </Descriptions.Item>
              <Descriptions.Item label="办结时间">
                {caseData.completedAt ? dayjs(caseData.completedAt).format('YYYY-MM-DD HH:mm:ss') : '-'}
              </Descriptions.Item>
              <Descriptions.Item label="撤回时间">
                {caseData.withdrawnAt ? dayjs(caseData.withdrawnAt).format('YYYY-MM-DD HH:mm:ss') : '-'}
              </Descriptions.Item>
              <Descriptions.Item label="备注">{caseData.remark || '-'}</Descriptions.Item>
            </Descriptions>
          </Card>

          <Card title="已提交材料" style={{ marginBottom: 16 }}>
            {caseData.submittedMaterials && caseData.submittedMaterials.length > 0 ? (
              <Table
                columns={materialColumns}
                dataSource={caseData.submittedMaterials}
                rowKey="materialId"
                pagination={false}
              />
            ) : (
              <Alert message="暂无已提交材料" type="info" />
            )}
          </Card>

          {caseData.missingMaterialIds && caseData.missingMaterialIds.length > 0 && (
            <Card title="缺失材料" style={{ marginBottom: 16 }}>
              <Alert
                message="以下材料缺失，需要补正"
                type="warning"
                showIcon
                style={{ marginBottom: 16 }}
              />
              <List
                dataSource={caseData.missingMaterialIds}
                renderItem={(item, index) => (
                  <List.Item>
                    <Tag color="warning">缺失</Tag>
                    <span style={{ marginLeft: 8 }}>材料 ID: {item}</span>
                  </List.Item>
                )}
              />
            </Card>
          )}

          {supervisionRecords.length > 0 && (
            <Card title="督办记录" style={{ marginBottom: 16 }}>
              <Timeline>
                {supervisionRecords.map((record, index) => (
                  <Timeline.Item key={index} color="red">
                    <p>
                      <strong>督办时间:</strong> {dayjs(record.supervisionTime).format('YYYY-MM-DD HH:mm:ss')}
                    </p>
                    <p>
                      <strong>超期时长:</strong> {record.overdueHours} 小时
                    </p>
                    <p>
                      <strong>督办状态:</strong> <Tag color="default">{record.status}</Tag>
                    </p>
                    <p>
                      <strong>备注:</strong> {record.remark}
                    </p>
                  </Timeline.Item>
                ))}
              </Timeline>
            </Card>
          )}
        </>
      ) : (
        <Alert message="办件不存在" type="error" showIcon />
      )}

      <Modal
        title="受理办件"
        open={acceptModalVisible}
        onCancel={() => setAcceptModalVisible(false)}
        footer={null}
      >
        <Form form={form} layout="vertical" onFinish={handleAccept}>
          <Form.Item
            name="handler"
            label="经办人"
            rules={[{ required: true, message: '请输入经办人' }]}
          >
            <Select placeholder="请选择经办人">
              <Option value="staff1">张三</Option>
              <Option value="staff2">李四</Option>
              <Option value="staff3">王五</Option>
            </Select>
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">
                确认受理
              </Button>
              <Button onClick={() => setAcceptModalVisible(false)}>取消</Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="材料补正"
        open={supplementModalVisible}
        onCancel={() => setSupplementModalVisible(false)}
        footer={null}
      >
        <Form layout="vertical" onFinish={handleSupplement}>
          <Alert
            message="补正说明"
            description="点击确认后将模拟补正所有缺失材料，实际系统中应提供文件上传功能。"
            type="info"
            showIcon
            style={{ marginBottom: 16 }}
          />
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">
                确认补正
              </Button>
              <Button onClick={() => setSupplementModalVisible(false)}>取消</Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="审批处理"
        open={approveModalVisible}
        onCancel={() => setApproveModalVisible(false)}
        footer={null}
        width={600}
      >
        <Form form={form} layout="vertical" onFinish={handleApprove}>
          <Form.Item
            name="approver"
            label="审批人"
            rules={[{ required: true, message: '请输入审批人' }]}
          >
            <Select placeholder="请选择审批人">
              <Option value="approver1">张审批</Option>
              <Option value="approver2">李审批</Option>
            </Select>
          </Form.Item>
          <Form.Item
            name="result"
            label="审批结果"
            rules={[{ required: true, message: '请选择审批结果' }]}
          >
            <Select placeholder="请选择审批结果">
              <Option value="PASS">通过</Option>
              <Option value="REJECT">驳回</Option>
              <Option value="RETURN_TO_SUPPLEMENT">退回补正</Option>
            </Select>
          </Form.Item>
          <Form.Item name="comment" label="审批意见">
            <TextArea rows={4} placeholder="请输入审批意见（可选）" />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">
                确认审批
              </Button>
              <Button onClick={() => setApproveModalVisible(false)}>取消</Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="撤回办件"
        open={withdrawModalVisible}
        onCancel={() => setWithdrawModalVisible(false)}
        footer={null}
      >
        <Form form={form} layout="vertical" onFinish={handleWithdraw}>
          <Form.Item name="reason" label="撤回原因">
            <TextArea rows={4} placeholder="请输入撤回原因（可选）" />
          </Form.Item>
          <Alert
            message="确认撤回该办件？"
            description="撤回后无法恢复，请谨慎操作。"
            type="warning"
            showIcon
            style={{ marginBottom: 16 }}
          />
          <Form.Item>
            <Space>
              <Button type="primary" danger htmlType="submit">
                确认撤回
              </Button>
              <Button onClick={() => setWithdrawModalVisible(false)}>取消</Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="生成电子证照/结果文件"
        open={documentModalVisible}
        onCancel={() => setDocumentModalVisible(false)}
        footer={null}
        width={600}
      >
        <Form form={documentForm} layout="vertical" onFinish={handleGenerateDocument}>
          <Form.Item
            name="documentType"
            label="文档类型"
            rules={[{ required: true, message: '请选择文档类型' }]}
            initialValue="ELICENSE"
          >
            <Select placeholder="请选择文档类型">
              <Option value="ELICENSE">电子证照</Option>
              <Option value="RESULT">结果文件</Option>
            </Select>
          </Form.Item>
          <Form.Item name="content" label="文档内容">
            <TextArea rows={6} placeholder="请输入文档内容（留空则生成默认内容）" />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">
                生成文档
              </Button>
              <Button onClick={() => setDocumentModalVisible(false)}>取消</Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="确认手动超期"
        open={forceOverdueConfirmVisible}
        onCancel={() => setForceOverdueConfirmVisible(false)}
        footer={null}
      >
        <Alert
          message="确认将该办件标记为超期？"
          description="此操作将：1) 将到期时间设为昨天；2) 标记为超期状态；3) 生成督办记录。此操作不可撤销。"
          type="warning"
          showIcon
          style={{ marginBottom: 24 }}
        />
        <Space>
          <Button
            type="primary"
            danger
            onClick={handleForceOverdue}
          >
            确认超期
          </Button>
          <Button onClick={() => setForceOverdueConfirmVisible(false)}>
            取消
          </Button>
        </Space>
      </Modal>
    </div>
  )
}

export default CaseDetail

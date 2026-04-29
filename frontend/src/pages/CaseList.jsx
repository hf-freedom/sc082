import React, { useState, useEffect } from 'react'
import { Table, Tag, Space, Button, Input, Select, Card, Row, Col, Statistic } from 'antd'
import {
  SearchOutlined,
  PlusOutlined,
  EyeOutlined,
  FileTextOutlined,
  ClockCircleOutlined,
  CheckCircleOutlined,
  WarningOutlined,
} from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { caseApi, itemApi } from '../api'
import dayjs from 'dayjs'

const { Search } = Input
const { Option } = Select

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

function CaseList() {
  const navigate = useNavigate()
  const [cases, setCases] = useState([])
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(false)
  const [searchText, setSearchText] = useState('')
  const [statusFilter, setStatusFilter] = useState(undefined)
  const [itemFilter, setItemFilter] = useState(undefined)

  const loadCases = async () => {
    setLoading(true)
    try {
      const data = await caseApi.list()
      setCases(data || [])
    } catch (error) {
      console.error('加载办件列表失败:', error)
    } finally {
      setLoading(false)
    }
  }

  const loadItems = async () => {
    try {
      const data = await itemApi.listActive()
      setItems(data || [])
    } catch (error) {
      console.error('加载事项列表失败:', error)
    }
  }

  useEffect(() => {
    loadCases()
    loadItems()
  }, [])

  const getStatistics = () => {
    const total = cases.length
    const pendingAccept = cases.filter((c) => c.status === 'PENDING_ACCEPT').length
    const inApproval = cases.filter((c) => c.status === 'IN_APPROVAL').length
    const overdue = cases.filter((c) => c.status === 'OVERDUE').length
    const completed = cases.filter((c) => c.status === 'COMPLETED').length

    return { total, pendingAccept, inApproval, overdue, completed }
  }

  const stats = getStatistics()

  const filteredCases = cases.filter((c) => {
    let match = true
    if (searchText) {
      const text = searchText.toLowerCase()
      match =
        c.caseNumber?.toLowerCase().includes(text) ||
        c.applicantName?.toLowerCase().includes(text) ||
        c.itemName?.toLowerCase().includes(text)
    }
    if (match && statusFilter) {
      match = c.status === statusFilter
    }
    if (match && itemFilter) {
      match = c.itemId === itemFilter
    }
    return match
  })

  const columns = [
    {
      title: '案件编号',
      dataIndex: 'caseNumber',
      key: 'caseNumber',
      width: 180,
      render: (text) => <a onClick={() => navigate(`/cases/${cases.find((c) => c.caseNumber === text)?.id}`)}>{text}</a>,
    },
    {
      title: '事项名称',
      dataIndex: 'itemName',
      key: 'itemName',
      width: 150,
    },
    {
      title: '申请人',
      dataIndex: 'applicantName',
      key: 'applicantName',
      width: 100,
    },
    {
      title: '联系电话',
      dataIndex: 'applicantPhone',
      key: 'applicantPhone',
      width: 120,
    },
    {
      title: '当前状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status) => {
        const info = statusMap[status] || { text: status, color: 'default' }
        return <Tag color={info.color}>{info.text}</Tag>
      },
    },
    {
      title: '当前节点',
      dataIndex: 'currentNodeName',
      key: 'currentNodeName',
      width: 100,
      render: (text) => text || '-',
    },
    {
      title: '经办人',
      dataIndex: 'handler',
      key: 'handler',
      width: 100,
      render: (text) => text || '-',
    },
    {
      title: '提交时间',
      dataIndex: 'submittedAt',
      key: 'submittedAt',
      width: 160,
      render: (time) => (time ? dayjs(time).format('YYYY-MM-DD HH:mm') : '-'),
    },
    {
      title: '到期时间',
      dataIndex: 'dueDate',
      key: 'dueDate',
      width: 160,
      render: (time) => (time ? dayjs(time).format('YYYY-MM-DD HH:mm') : '-'),
    },
    {
      title: '操作',
      key: 'action',
      width: 100,
      render: (_, record) => (
        <Space size="middle">
          <Button type="link" icon={<EyeOutlined />} onClick={() => navigate(`/cases/${record.id}`)}>
            查看
          </Button>
        </Space>
      ),
    },
  ]

  return (
    <div>
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={4}>
          <Card>
            <Statistic
              title="总办件数"
              value={stats.total}
              prefix={<FileTextOutlined />}
            />
          </Card>
        </Col>
        <Col span={4}>
          <Card>
            <Statistic
              title="待受理"
              value={stats.pendingAccept}
              valueStyle={{ color: '#fa8c16' }}
              prefix={<ClockCircleOutlined />}
            />
          </Card>
        </Col>
        <Col span={4}>
          <Card>
            <Statistic
              title="审批中"
              value={stats.inApproval}
              valueStyle={{ color: '#722ed1' }}
              prefix={<ClockCircleOutlined />}
            />
          </Card>
        </Col>
        <Col span={4}>
          <Card>
            <Statistic
              title="超期办件"
              value={stats.overdue}
              valueStyle={{ color: '#ff4d4f' }}
              prefix={<WarningOutlined />}
            />
          </Card>
        </Col>
        <Col span={4}>
          <Card>
            <Statistic
              title="已办结"
              value={stats.completed}
              valueStyle={{ color: '#52c41a' }}
              prefix={<CheckCircleOutlined />}
            />
          </Card>
        </Col>
        <Col span={4}>
          <Card>
            <Statistic
              title="办结率"
              value={stats.total > 0 ? ((stats.completed / stats.total) * 100).toFixed(1) : 0}
              suffix="%"
              valueStyle={{ color: '#1890ff' }}
              prefix={<CheckCircleOutlined />}
            />
          </Card>
        </Col>
      </Row>

      <Row justify="space-between" style={{ marginBottom: 16 }}>
        <Space>
          <Search
            placeholder="搜索案件编号/申请人/事项名称"
            allowClear
            style={{ width: 300 }}
            prefix={<SearchOutlined />}
            onSearch={(value) => setSearchText(value)}
            onChange={(e) => setSearchText(e.target.value)}
          />
          <Select
            placeholder="状态筛选"
            allowClear
            style={{ width: 150 }}
            onChange={setStatusFilter}
          >
            {Object.entries(statusMap).map(([key, value]) => (
              <Option key={key} value={key}>
                {value.text}
              </Option>
            ))}
          </Select>
          <Select
            placeholder="事项筛选"
            allowClear
            style={{ width: 200 }}
            onChange={setItemFilter}
          >
            {items.map((item) => (
              <Option key={item.id} value={item.id}>
                {item.name}
              </Option>
            ))}
          </Select>
        </Space>
        <Space>
          <Button icon={<PlusOutlined />} type="primary" onClick={() => navigate('/cases/new')}>
            新建办件
          </Button>
          <Button onClick={loadCases}>刷新</Button>
        </Space>
      </Row>

      <Table
        columns={columns}
        dataSource={filteredCases}
        rowKey="id"
        loading={loading}
        scroll={{ x: 1400 }}
        pagination={{
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (total) => `共 ${total} 条记录`,
        }}
      />
    </div>
  )
}

export default CaseList
